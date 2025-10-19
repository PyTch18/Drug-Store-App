package com.example.drugstore.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.google.android.gms.location.LocationServices
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import androidx.compose.material.icons.filled.Search
import android.os.AsyncTask
import org.json.JSONObject
import org.maplibre.android.annotations.MarkerOptions
import java.net.URL

data class Pharmacy(val name: String, val lat: Double, val lon: Double)

@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }

    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    var mapLibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            hasLocationPermission = true
        } else {
            Toast.makeText(context, "Location permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {

            // --- Add MapLibreMap ---
            MapLibreMap(
                modifier = Modifier.fillMaxSize(),
                onMapCreated = { mapView, maplibreMap ->
                    mapViewInstance = mapView
                    mapLibreMapInstance = maplibreMap

                    if (hasLocationPermission) {
                        getCurrentLocation(context) { userLocation ->
                            maplibreMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(userLocation, 14.0)
                            )
                        }
                    }
                }
            )

            // --- Add Search Bar at top ---
            MapSearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter) // Top of the screen
                    .padding(top = 16.dp),
                onSearch = { query ->
                    val map = mapLibreMapInstance ?: return@MapSearchBar
                    getCurrentLocation(context) { userLocation ->
                        fetchNearbyPharmacies(query, userLocation) { pharmacies ->
                            // Clear existing markers
                            map.clear()
                            // Add markers for each pharmacy
                            pharmacies.forEach { pharmacy ->
                                val markerOptions = MarkerOptions()
                                    .position(LatLng(pharmacy.lat, pharmacy.lon))
                                    .title(pharmacy.name)
                                map.addMarker(markerOptions)                            }
                            // Move camera to first pharmacy or user location
                            val target = pharmacies.firstOrNull()?.let { LatLng(it.lat, it.lon) }
                                ?: userLocation
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 14.0))
                        }
                    }
                }
            )

            // --- "My Location" Button ---
            if (hasLocationPermission) {
                FloatingActionButton(
                    onClick = {
                        mapLibreMapInstance?.let { map ->
                            getCurrentLocation(context) { userLocation ->
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(userLocation, 16.0)
                                )
                            }
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
fun MapLibreMap(
    modifier: Modifier = Modifier,
    onMapCreated: (MapView, MapLibreMap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Your Maptiler API key.
    // For better security, move this to build.gradle or a local properties file later.
    val apiKey = "zzgmqsVJut1EzyQjPOwI"
    val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=$apiKey"

    // Use remember to create the MapView only once
    val mapView = remember {
        // Initialize MapLibre with the application context.
        MapLibre.getInstance(context)
        MapView(context)
    }

    // Use AndroidView to embed the MapView in Compose
    AndroidView(
        factory = {
            mapView.apply {
                // Get the map asynchronously
                getMapAsync { maplibreMap ->
                    // Load the Maptiler style
                    maplibreMap.setStyle(styleUrl) {
                        // Style is loaded, now the map is fully ready.
                        onMapCreated(mapView, maplibreMap)
                        Log.d("MapLibre", "Map style loaded successfully.")
                    }
                }
            }
        },
        modifier = modifier
    )

    // Manage the MapView's lifecycle
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

// Helper function to get the current location. This function remains the same.
@SuppressLint("MissingPermission")
private fun getCurrentLocation(context: Context, onLocation: (LatLng) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            onLocation(LatLng(location.latitude, location.longitude))
        } else {
            Log.d("MapLibre", "Last known location is null.")
            // You can optionally request a new location update here if needed.
        }
    }.addOnFailureListener { e ->
        Log.e("MapLibre", "Failed to get location: ${e.message}")
    }
}

@Composable
fun MapSearchBar(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search for an area") },
            modifier = Modifier.weight(1f), // This makes the text field expand
            singleLine = true,
        )

        IconButton(
            onClick = {
                // Trigger the search only if the user has typed something
                if (searchQuery.isNotBlank()) {
                    onSearch(searchQuery)
                }
            },
            modifier = Modifier.padding(start = 8.dp) // Space between field and button
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        }
    }
}

fun fetchNearbyPharmacies(
    query: String,
    location: LatLng,
    onResult: (List<Pharmacy>) -> Unit
) {
    AsyncTask.execute {
        try {
            val radius = 2000 // meters
            val overpassUrl =
                "https://overpass-api.de/api/interpreter?data=[out:json];" +
                        "node[amenity=pharmacy](around:$radius,${location.latitude},${location.longitude});out;"
            val jsonText = URL(overpassUrl).readText()
            val json = JSONObject(jsonText)
            val elements = json.getJSONArray("elements")
            val pharmacies = mutableListOf<Pharmacy>()
            for (i in 0 until elements.length()) {
                val elem = elements.getJSONObject(i)
                val lat = elem.getDouble("lat")
                val lon = elem.getDouble("lon")
                val tags = elem.optJSONObject("tags")
                val name = tags?.optString("name") ?: "Unnamed Pharmacy"
                pharmacies.add(Pharmacy(name, lat, lon))
            }
            onResult(pharmacies)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(emptyList())
        }
    }
}