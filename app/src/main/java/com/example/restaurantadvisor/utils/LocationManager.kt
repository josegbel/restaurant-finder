package com.example.restaurantadvisor.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationManager(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    private val onLocationUpdate: (Double, Double) -> Unit
) : DefaultLifecycleObserver {

    private lateinit var locationCallback: LocationCallback
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private var locationRequest: LocationRequest

    init {
        lifecycleOwner.lifecycle.addObserver(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL).build()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d("com.example.restaurantadvisor.utils.LocationManager", "onStart")
        setupLocationCallback()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        stopLocationUpdates()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latLong = "${location.latitude},${location.longitude}"
                    Log.d("com.example.restaurantadvisor.utils.LocationManager", "Location update: $latLong")
                    onLocationUpdate(location.latitude, location.longitude)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        Log.d("com.example.restaurantadvisor.utils.LocationManager", "Starting location updates")
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

        // Immediately try to get the last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latLong = "${it.latitude},${it.longitude}"
                Log.d("com.example.restaurantadvisor.utils.LocationManager", "Last known location: $latLong")
                onLocationUpdate(it.latitude, it.longitude)
            } ?: Log.d("com.example.restaurantadvisor.utils.LocationManager", "No last known location found")
        }.addOnFailureListener {
            Log.e("com.example.restaurantadvisor.utils.LocationManager", "Failed to get last known location")
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val INTERVAL = 5_000L
    }
}
