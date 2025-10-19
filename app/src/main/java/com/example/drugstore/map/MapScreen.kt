package com.example.drugstore.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

@Composable
fun MapScreen() {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }

    var searchResults by remember { mutableStateOf<List<Pharmacy>>(emptyList()) }
    var selectedPharmacy by remember { mutableStateOf<Pharmacy?>(null) }
    var isSearching by remember { mutableStateOf(false) } // To show a loading indicator

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
            MapSearchBar { query ->
                Toast.makeText(context, "Searching for: $query", Toast.LENGTH_SHORT).show()
            }
        }
    ) { paddingValues ->
        HereMap(modifier = Modifier.padding(paddingValues))
    }
}

@Composable
fun HereMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = {
            initializeHereSdk(it)

            val mapView = MapView(it)

            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

            mapView.mapScene.loadScene(MapScheme.NORMAL_DAY) { mapError ->
                if (mapError == null) {
                    val defaultLocation = GeoCoordinates(30.0444, 31.2357) // Cairo
                    val distanceInMeters = MapMeasure(MapMeasure.Kind.DISTANCE_IN_METERS, 10000.0)
                    mapView.camera.lookAt(defaultLocation, distanceInMeters)
                    addMapMarker(mapView, defaultLocation)
                    Log.d("HereMap", "Map scene loaded successfully.")
                } else {
                    Log.e("HereMap", "Error loading map scene: $mapError")
                }
            }

            mapView
        },
        onRelease = {
            SDKNativeEngine.getSharedInstance()?.dispose()
            Log.d("HereMap", "HERE SDK instance disposed on view release.")
        },
        modifier = modifier.fillMaxSize()
    )
}


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

private fun addMapMarker(mapView: MapView, geoCoordinates: GeoCoordinates) {
    try {
        val mapImage = MapImageFactory.fromResource(mapView.context.resources, R.drawable.ic_launcher_foreground)
        val mapMarker = MapMarker(geoCoordinates, mapImage)
        mapView.mapScene.addMapMarker(mapMarker)
    } catch (e: Exception) {
        Log.e("HereMap", "Failed to add map marker: ${e.message}")
    }
}

@Composable
fun MapSearchBar(onSearch: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search for an area") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )

        IconButton(
            onClick = {
                if (searchQuery.isNotBlank()) {
                    onSearch(searchQuery)
                }
            },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        }
    }
}

@SuppressLint("MissingPermission")
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

        // Fix: Implement both onSearchCompleted and onSearchExtendedCompleted
        searchEngine.search(textQuery, searchOptions, object : SearchCallbackExtended {
            fun onSearchCompleted(searchError: SearchError?, places: List<Place>?) {
                // This method is kept for backward compatibility but might not be called
                // in newer versions if the extended one is present.
                // It's good practice to handle the results here as a fallback.
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

            // This is the newly required method you must implement.
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
