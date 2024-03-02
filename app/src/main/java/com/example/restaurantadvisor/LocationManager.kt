import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.*

private const val INTERVAL: Long = 5_000

class LocationManager(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    private val onLocationUpdate: (Double, Double) -> Unit
) : DefaultLifecycleObserver {

    private lateinit var locationCallback: LocationCallback
    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationRequest: LocationRequest

    init {
        lifecycleOwner.lifecycle.addObserver(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL)
            .build()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d("LocationManager", "onStart")
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
                    Log.d("LocationManager", "Location update: $latLong")
                    onLocationUpdate(location.latitude, location.longitude)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        Log.d("LocationManager", "Starting location updates")
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)

        // Immediately try to get the last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latLong = "${it.latitude},${it.longitude}"
                Log.d("LocationManager", "Last known location: $latLong")
                onLocationUpdate(it.latitude, it.longitude)
            } ?: Log.d("LocationManager", "No last known location found")
        }.addOnFailureListener {
            Log.e("LocationManager", "Failed to get last known location")
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
