package com.example.drugstore.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.drugstore.R
import com.google.android.gms.location.LocationServices
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.mapview.*
import com.here.sdk.search.*
import com.here.sdk.core.LanguageCode
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.ui.geometry.isEmpty
import com.here.sdk.core.GeoBox
import com.here.sdk.search.SearchEngine
import com.here.sdk.search.SearchOptions
import com.here.sdk.search.TextQuery
import com.here.sdk.search.SearchCallbackExtended
import com.here.sdk.search.SearchError
import com.here.sdk.search.Place




data class Pharmacy(
    val title: String,
    val address: String,
    val coordinates: GeoCoordinates
)

@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pharmacy>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        ) {
            hasLocationPermission = true
        } else {
            Toast.makeText(context, "Location permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            MapSearchBar(
                query = searchQuery,
                onQueryChanged = { newQuery -> searchQuery = newQuery },
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        isSearching = true
                        searchForPharmacies(context, searchQuery, mapViewInstance!!.camera.state.targetCoordinates) { places ->
                            searchResults = places
                            isSearching = false
                            if (searchResults.isEmpty()) {
                                Toast.makeText(context, "No pharmacies found for '$searchQuery'", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            HereMap(
                modifier = Modifier.fillMaxSize(),
                pharmacies = searchResults,
                // Pass the lambda to get the MapView instance once it's created.
                onMapCreated = { mapView ->
                    mapViewInstance = mapView
                    // When the map is first ready, move to the user's current location.
                    if (hasLocationPermission) {
                        getCurrentLocation(context) { userLocation ->
                            mapView.camera.lookAt(userLocation, MapMeasure(MapMeasure.Kind.DISTANCE_IN_METERS, 5000.0))
                        }
                    }
                }
            )
            if (isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            if (hasLocationPermission) {
                FloatingActionButton(
                    onClick = {
                        getCurrentLocation(context) { userLocation ->
                            mapViewInstance!!.camera.lookAt(userLocation, MapMeasure(MapMeasure.Kind.DISTANCE_IN_METERS, 2000.0))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location")
                }
            }
        }
    }
}


@Composable
fun HereMap(
    modifier: Modifier = Modifier,
    pharmacies: List<Pharmacy>,
    onMapCreated: (MapView) -> Unit // The new callback parameter.
) {
    val mapMarkers = remember { mutableListOf<MapMarker>() }

    AndroidView(
        factory = { context ->
            initializeHereSdk(context)
            val mapView = MapView(context)
            mapView.onCreate(null)
            onMapCreated(mapView) // Send the created instance back up to the parent.
            mapView
        },
        update = { view ->
            // --- FIX STARTS HERE ---

            // 1. Remove the old markers that are currently on the map.
            view.mapScene.removeMapMarkers(mapMarkers)

            // 2. Clear your local list of marker references.
            mapMarkers.clear()

            // 3. Add the new markers for the updated pharmacy list.
            pharmacies.forEach { pharmacy ->
                addMapMarker(view, pharmacy.coordinates)?.let { marker ->
                    mapMarkers.add(marker)
                }
            }

            if (pharmacies.isNotEmpty()) {
                if (pharmacies.size == 1) {
                    view.camera.lookAt(pharmacies.first().coordinates, MapMeasure(MapMeasure.Kind.DISTANCE_IN_METERS, 2000.0))
                } else {
                    view.camera.lookAt(GeoBox.containing(pharmacies.map { it.coordinates }))
                }
            } else {
                if (mapMarkers.isEmpty()) {
                    val defaultLocation = GeoCoordinates(30.0444, 31.2357)
                    val distance = MapMeasure(MapMeasure.Kind.DISTANCE_IN_METERS, 10000.0)
                    view.camera.lookAt(defaultLocation, distance)
                }
            }
        },
        modifier = modifier
    )

    // This lifecycle management is simplified and correct.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        onDispose {
            // Clean up the SDK engine when the composable is removed.
            SDKNativeEngine.getSharedInstance()?.dispose()
            Log.d("HereMap", "HERE SDK instance disposed.")
        }
    }
}

private fun MapCamera.lookAt(p0: GeoBox?) {}


private fun initializeHereSdk(context: Context) {
    try {
        if (SDKNativeEngine.getSharedInstance() == null) {
            val accessKeyId = "MldG0F9XjjAl8uqYUzR0jA"
            val accessKeySecret = "m6QBHBWJ5aJsTjxyLQA20lnZiuP94NmRJfVbZxLBYWhsb1GdOEkqpAtbNFAWLx2Xh9zGg4anHwXC0pdXpgIoDw"

            if (accessKeyId.isBlank() || accessKeySecret.isBlank()) {
                throw RuntimeException("Hard-coded HERE SDK credentials are blank.")
            }

            val authMode = AuthenticationMode.withKeySecret(accessKeyId, accessKeySecret)
            val options = SDKOptions(authMode)

            SDKNativeEngine.makeSharedInstance(context, options)
            Log.d("HereMap", "HERE SDK initialized.")
        }
    } catch (e: Exception) {
        Log.e("HereMap", "CRITICAL: Failed to initialize HERE SDK: ${e.message}", e)
        Toast.makeText(context, "Map services failed to start. See Logcat for details.", Toast.LENGTH_LONG).show()
    }
}

private fun addMapMarker(mapView: MapView, geoCoordinates: GeoCoordinates): MapMarker? {
    try {
        val mapImage = MapImageFactory.fromResource(mapView.context.resources, R.drawable.ic_launcher_foreground)
        val mapMarker = MapMarker(geoCoordinates, mapImage)
        mapView.mapScene.addMapMarker(mapMarker)
        return mapMarker // Return the created marker
    } catch (e: Exception) {
        Log.e("HereMap", "Failed to add map marker: ${e.message}")
        return null // Return null if creation fails
    }
}

@Composable
fun MapSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit // A simple lambda to trigger the search
) {    Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged, // Pass text changes up to the parent
        label = { Text("Search for an area") },
        modifier = Modifier.weight(1f),
        singleLine = true,
    )
    IconButton(
        onClick = onSearch, // The button now calls the onSearch lambda
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search"
        )
    }
}
}


@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
private fun getCurrentLocation(context: Context, onLocation: (GeoCoordinates) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val userLocation = GeoCoordinates(location.latitude, location.longitude)
            onLocation(userLocation)
        } else {
            Log.d("HereMap", "Last known location is null. Requesting new location.")
            onLocation(GeoCoordinates(30.0444, 31.2357)) // Default to Cairo
        }
    }.addOnFailureListener { e ->
        Log.e("HereMap", "Failed to get location: ${e.message}")
        onLocation(GeoCoordinates(30.0444, 31.2357)) // fallback
    }
}

private fun searchForPharmacies(
    context: Context,    query: String,
    searchArea: GeoCoordinates,
    onResult: (List<Pharmacy>) -> Unit
) {
    try {
        val searchEngine = SearchEngine()

        val queryArea = TextQuery.Area(searchArea)
        val textQuery = TextQuery("pharmacy near $query", queryArea)

        val searchOptions = SearchOptions().apply {
            languageCode = LanguageCode.EN_US
            maxItems = 30
        }

        searchEngine.search(textQuery, searchOptions, object : SearchCallbackExtended {
              fun onSearchCompleted(searchError: SearchError?, places: List<Place>?) {
                if (searchError != null) {
                    Log.e("HereMap", "Search error: ${searchError.name}")
                    onResult(emptyList())
                    return
                }

                val pharmacies = places?.mapNotNull { place ->
                    place.geoCoordinates?.let { coords ->
                        Pharmacy(
                            title = place.title ?: "Unnamed Pharmacy",
                            address = place.address?.addressText ?: "No address",
                            coordinates = coords
                        )
                    }
                } ?: emptyList()

                onResult(pharmacies)
            }

             fun onSearchExtendedCompleted(
                searchError: SearchError?,
                places: List<Place>?,
                suggestedQuery: String?
            ) {
                if (searchError != null) {
                    Log.e("HereMap", "Search (extended) error: ${searchError.name}")
                    Toast.makeText(context, "Search error: ${searchError.name}", Toast.LENGTH_SHORT).show()
                    onResult(emptyList())
                    return
                }

                val pharmacies = places?.mapNotNull { place ->
                    place.geoCoordinates?.let { coords ->
                        Pharmacy(
                            title = place.title ?: "Unnamed Pharmacy",
                            address = place.address?.addressText ?: "No address",
                            coordinates = coords
                        )
                    }
                } ?: emptyList()

                onResult(pharmacies)
            }

            override fun onSearchExtendedCompleted(
                p0: SearchError?,
                p1: List<Place?>?,
                p2: ResponseDetails?
            ) {
                TODO("Not yet implemented")
            }
        })
    } catch (e: Exception) {
        Log.e("HereMap", "Failed to create or use SearchEngine: ${e.message}", e)
        onResult(emptyList())
    }
}
