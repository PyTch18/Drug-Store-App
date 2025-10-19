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
        // Pass the padding to HereMap
        HereMap(modifier = Modifier.padding(paddingValues))
    }
}

@Composable
fun HereMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // --- THIS IS THE CRUCIAL FIX ---
    // We create the MapView inside a remember block and pass it to DisposableEffect
    val mapView = remember {
        // We still initialize the SDK first for safety
        initializeHereSdk(context)
        MapView(context)
    }

    // This effect will tie the MapView's lifecycle to the composable's lifecycle
    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            // Forward lifecycle events to the MapView
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null) // Call onCreate here
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }

        lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
            // It's still good practice to dispose the engine here
            SDKNativeEngine.getSharedInstance()?.dispose()
            Log.d("HereMap", "HERE SDK instance disposed.")
        }
    }

    AndroidView(
        factory = { mapView }, // The factory now just returns the already-created view
        update = { view ->
            // This is where you would update the view on recomposition if needed
            Log.d("HereMap", "AndroidView update block.")

            // It's safer to load the scene here as part of the update/setup logic
            view.mapScene.loadScene(MapScheme.NORMAL_DAY) { mapError ->
                if (mapError == null) {
                    val defaultLocation = GeoCoordinates(30.0444, 31.2357) // Cairo
                    val distanceInMeters = MapMeasure(MapMeasure.Kind.DISTANCE_IN_METERS, 10000.0)
                    view.camera.lookAt(defaultLocation, distanceInMeters)
                    addMapMarker(view, defaultLocation)
                    Log.d("HereMap", "Map scene loaded successfully.")
                } else {
                    Log.e("HereMap", "Error loading map scene: $mapError")
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}


// Helper function to initialize the SDK
// In MapScreen.kt

private fun initializeHereSdk(context: Context) {    try {
    if (SDKNativeEngine.getSharedInstance() == null) {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        val metaData = appInfo.metaData // Get the bundle first


        val accessKeyId = "MldG0F9XjjAl8uqYUzR0jA"
        val accessKeySecret = "m6QBHBWJ5aJsTjxyLQA20lnZiuP94NmRJfVbZxLBYWhsb1GdOEkqpAtbNFAWLx2Xh9zGg4anHwXC0pdXpgIoDw"

        if (accessKeyId.isNullOrBlank() || accessKeySecret.isNullOrBlank()) {
            throw RuntimeException("HERE SDK credentials are null or blank in AndroidManifest.xml")
        }

        val authMode = AuthenticationMode.withKeySecret(accessKeyId, accessKeySecret)
        val options = SDKOptions(authMode)

        SDKNativeEngine.makeSharedInstance(context, options)
        Log.d("HereMap", "HERE SDK initialized.")
    }
} catch (e: Exception) {
    // This will now give a much clearer error message in Logcat
    Log.e("HereMap", "Failed to initialize HERE SDK: ${e.message}", e)
    Toast.makeText(context, "Failed to initialize map services. Check Manifest and credentials.", Toast.LENGTH_LONG).show()
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
