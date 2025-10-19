package com.example.drugstore.ui.map

import android.Manifest
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.drugstore.R
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapMeasure.Kind
import com.here.sdk.core.engine.AuthenticationMode

@Composable
fun MapScreen() {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }

    // This will be used to request location permissions
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

    // Request permissions when the screen is first composed
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
                // Search logic will be passed here, but is not yet implemented
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
    val mapView = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle, mapView) {        val lifecycleObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            else -> {}
        }
    }
        lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            mapView.onDestroy()
            SDKNativeEngine.getSharedInstance()?.dispose()
            Log.d("HereMap", "HERE SDK instance disposed.")
        }
    }

    AndroidView(
        factory = {
            Log.d("HereMap", "Initializing HERE MapView...")
            initializeHereSdk(context)
            mapView.apply {
                mapScene.loadScene(MapScheme.NORMAL_DAY) { mapError ->
                    if (mapError == null) {
                        val defaultLocation = GeoCoordinates(30.0444, 31.2357) // Cairo

                        // --- THIS IS THE CORRECT AND FINAL IMPLEMENTATION ---
                        // The HERE SDK requires a MapMeasure object for distance.
                        // We must use this version of the lookAt function.
                        val distanceInMeters = MapMeasure(MapMeasure.Kind.DISTANCE_IN_METERS, 10000.0)
                        mapView.camera.lookAt(defaultLocation, distanceInMeters)
                        // --- END OF FIX ---

                        addMapMarker(mapView, defaultLocation)
                        Log.d("HereMap", "Map scene loaded successfully.")
                    }  else {
                        Log.e("HereMap", "Error loading map scene: $mapError")
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}



// Helper function to initialize the SDK
private fun initializeHereSdk(context: Context) {
    try {
        if (SDKNativeEngine.getSharedInstance() == null) {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
            val accessKeyId: String? = appInfo.metaData.getString("com.here.sdk.access_key_id")
            val accessKeySecret: String? = appInfo.metaData.getString("com.here.sdk.access_key_secret")

            if (accessKeyId.isNullOrBlank() || accessKeySecret.isNullOrBlank()) {
                throw RuntimeException("HERE SDK credentials not found in AndroidManifest.xml")
            }
            val authMode = AuthenticationMode.withKeySecret(accessKeyId, accessKeySecret)
            val options = SDKOptions(authMode)

            SDKNativeEngine.makeSharedInstance(context, options)
            Log.d("HereMap", "HERE SDK initialized.")
        }
    } catch (e: Exception) {
        Log.e("HereMap", "Failed to initialize HERE SDK: ${e.message}", e)
        Toast.makeText(context, "Failed to initialize map services. Check credentials.", Toast.LENGTH_LONG).show()
    }
}

// Helper function to add markers
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

    OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        label = { Text("Search for an area") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        singleLine = true,
    )
}
