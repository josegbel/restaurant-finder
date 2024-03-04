@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.restaurantadvisor.ui

import RestaurantDetailsScreen
import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.restaurantadvisor.data.db.AppDatabase
import com.example.restaurantadvisor.data.network.RestaurantService
import com.example.restaurantadvisor.data.repository.RestaurantRepositoryImpl
import com.example.restaurantadvisor.model.Location
import com.example.restaurantadvisor.ui.viewmodels.Error.NETWORK_ERROR
import com.example.restaurantadvisor.ui.viewmodels.Error.REQUIRE_LOCATION_PERMISSION
import com.example.restaurantadvisor.ui.viewmodels.MainViewModel.UiEvent
import com.example.restaurantadvisor.ui.composable.MainScreen
import com.example.restaurantadvisor.ui.theme.RestaurantAdvisorTheme
import com.example.restaurantadvisor.ui.viewmodels.DetailsViewModel
import com.example.restaurantadvisor.ui.viewmodels.DetailsViewModelFactory
import com.example.restaurantadvisor.ui.viewmodels.MainViewModel
import com.example.restaurantadvisor.ui.viewmodels.MainViewModelFactory
import com.example.restaurantadvisor.utils.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationManager: LocationManager

    private lateinit var mainViewModel: MainViewModel
    private lateinit var detailsViewModel: DetailsViewModel

    override fun onStart() {
        super.onStart()

        requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                mainViewModel.updatePermissionState(isGranted = true)
            } else {
                val shouldShowRationale =
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                mainViewModel.updatePermissionState(
                    isGranted = false, shouldShowRationale = shouldShowRationale
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "database-name"
        ).build()
        val api = RestaurantService()

        requestViewModels(api, db)

        setContent {
            RestaurantAdvisorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(mainViewModel, detailsViewModel)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.uiState.collect { uiState ->

                    if (uiState.locPermissionGranted) {
                        Log.d(TAG, "Location permission granted")
                        locationManager.startLocationUpdates()
                    } else {
                        mainViewModel.requestLocationPermission()
                    }

                    if(uiState.locPermissionGranted) {
                        locationManager =
                            LocationManager(this@MainActivity, this@MainActivity) { lat, long ->
                                val isNewLocation =
                                    lat != mainViewModel.location.value?.lat && long != mainViewModel.location.value?.long
                                if (isNewLocation) {
                                    mainViewModel.fetchNearbyRestaurants(latLong = "$lat,$long")
                                    mainViewModel.updateLocation(Location(lat, long))
                                }
                            }
                    }

                    when (uiState.error) {
                        REQUIRE_LOCATION_PERMISSION -> {
                            Log.e(TAG, "Location permission required")
                            showToast("Location permission required")
                        }

                        NETWORK_ERROR -> {
                            Log.e(TAG, "Network error occurred")
                            showToast("Network error occurred")
                            // clear the error from uiState
                            mainViewModel.updateError(null)
                        }

                        else -> {
                            // do nothing
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.events.collect { event ->
                    when (event) {
                        UiEvent.RequestPermission -> {
                            Log.d(TAG, "Requesting location permission")
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                }
            }
        }
    }

    private fun requestViewModels(
        api: RestaurantService,
        db: AppDatabase
    ) {
        val mainViewModelFactory = MainViewModelFactory(
            RestaurantRepositoryImpl(api, db, Dispatchers.IO)
        )
        mainViewModel = ViewModelProvider(this, mainViewModelFactory)
            .get(MainViewModel::class.java)

        val detailsViewModelFactory = DetailsViewModelFactory(
            RestaurantRepositoryImpl(api, db, Dispatchers.IO)
        )
        detailsViewModel = ViewModelProvider(this, detailsViewModelFactory)
            .get(DetailsViewModel::class.java)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}


@Composable
fun AppContent(mainViewModel: MainViewModel, detailsViewModel: DetailsViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(mainViewModel,detailsViewModel, navController) }
        composable("restaurant_details") { RestaurantDetailsScreen(detailsViewModel, navController) }
    }
}



