package com.example.drugstore.ui.map

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

private const val PHARMACY_SOURCE_ID = "pharmacy-source"
private const val PHARMACY_LAYER_ID = "pharmacy-layer"
private const val USER_SOURCE_ID = "user-source"
private const val USER_LAYER_ID = "user-layer"
private const val MARKER_ICON = "marker-15"

@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isInitialCameraMoveDone by remember { mutableStateOf(false) }

    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val virtualPharmacies by viewModel.virtualPharmacies.collectAsStateWithLifecycle()

    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            hasLocationPermission = true
            viewModel.startLocationUpdates()
        } else {
            Toast.makeText(context, "Location permission required to show your position.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Scaffold {
        Box(modifier = Modifier.padding(it).fillMaxSize()) {
            MapLibreMapView(
                modifier = Modifier.fillMaxSize(),
                onMapReady = { maplibreMap, loadedStyle ->
                    map = maplibreMap
                    style = loadedStyle
                }
            )

            if (hasLocationPermission) {
                FloatingActionButton(
                    onClick = {
                        userLocation?.let { loc ->
                            map?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 14.0)
                            )
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.MyLocation, "My Location")
                }
            }
        }
    }

    LaunchedEffect(style, userLocation) {
        val s = style ?: return@LaunchedEffect
        val source = s.getSourceAs<GeoJsonSource>(USER_SOURCE_ID) ?: return@LaunchedEffect
        userLocation?.let {
            source.setGeoJson(Point.fromLngLat(it.longitude, it.latitude))
            if (!isInitialCameraMoveDone) {
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 13.0)
                )
                isInitialCameraMoveDone = true
            }
        }
    }

    LaunchedEffect(style, virtualPharmacies) {
        val s = style ?: return@LaunchedEffect
        val source = s.getSourceAs<GeoJsonSource>(PHARMACY_SOURCE_ID) ?: return@LaunchedEffect

        val features = virtualPharmacies.map { pharmacy ->
            Feature.fromGeometry(Point.fromLngLat(pharmacy.long, pharmacy.lat)).apply {
                addStringProperty("name", pharmacy.name)
            }
        }
        source.setGeoJson(FeatureCollection.fromFeatures(features))
    }
}

@Composable
fun MapLibreMapView(
    modifier: Modifier = Modifier,
    onMapReady: (MapLibreMap, Style) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val apiKey = "zzgmqsVJut1EzyQjPOwI"
    val styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=$apiKey"

    val mapView = remember {
        // Initialize MapLibre before creating the MapView
        MapLibre.getInstance(context)
        MapView(context)
    }

    AndroidView({ mapView }, modifier) {
        it.getMapAsync { map ->
            map.setStyle(styleUrl) { style ->
                style.addSource(GeoJsonSource(PHARMACY_SOURCE_ID))
                style.addLayer(
                    SymbolLayer(PHARMACY_LAYER_ID, PHARMACY_SOURCE_ID).withProperties(
                        iconImage(MARKER_ICON),
                        iconAllowOverlap(true),
                        textField(get("name")),
                        textOffset(arrayOf(0f, -2.5f)),
                        textAnchor(org.maplibre.android.style.layers.Property.TEXT_ANCHOR_BOTTOM)
                    )
                )

                style.addSource(GeoJsonSource(USER_SOURCE_ID))
                style.addLayer(
                    SymbolLayer(USER_LAYER_ID, USER_SOURCE_ID).withProperties(
                        iconImage(MARKER_ICON),
                        iconAllowOverlap(true),
                        iconSize(1.5f)
                    )
                )
                onMapReady(map, style)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
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
