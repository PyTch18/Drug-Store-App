package com.example.drugstore.ui.map

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class PharmacyLocation(val lat: Double, val long: Double, val name: String)

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation = _userLocation.asStateFlow()

    private val _virtualPharmacies = MutableStateFlow<List<PharmacyLocation>>(emptyList())
    val virtualPharmacies = _virtualPharmacies.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private var havePharmaciesBeenGenerated = false

    fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModelScope.launch {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            _userLocation.value = location

                            // Generate pharmacies around the user, but only once.
                            if (!havePharmaciesBeenGenerated) {
                                generateRandomPharmaciesNear(location)
                                havePharmaciesBeenGenerated = true
                            }
                        }
                    }
            }
        }
    }

    private fun generateRandomPharmaciesNear(location: Location) {
        val pharmacyNames = listOf("Ezaby Pharmacy", "Misr Pharmacy", "19011 Pharmacy", "Seif Pharmacy")
        val randomPharmacies = pharmacyNames.map { name ->
            // Generate random coordinates in a small radius around the user's location
            val lat = location.latitude + (Random.nextDouble() * 0.02 - 0.01) // Approx 1.1km radius
            val long = location.longitude + (Random.nextDouble() * 0.02 - 0.01)
            PharmacyLocation(lat, long, name)
        }
        _virtualPharmacies.value = randomPharmacies
    }
}
