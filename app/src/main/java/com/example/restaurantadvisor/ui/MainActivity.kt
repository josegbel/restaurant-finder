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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.restaurantadvisor.R
import com.example.restaurantadvisor.R.string
import com.example.restaurantadvisor.data.db.AppDatabase
import com.example.restaurantadvisor.data.network.RestaurantService
import com.example.restaurantadvisor.data.repository.RestaurantRepositoryImpl
import com.example.restaurantadvisor.model.Location
import com.example.restaurantadvisor.ui.Error.NETWORK_ERROR
import com.example.restaurantadvisor.ui.Error.REQUIRE_LOCATION_PERMISSION
import com.example.restaurantadvisor.ui.MainViewModel.UiEvent
import com.example.restaurantadvisor.ui.composable.FoundRestaurantItem
import com.example.restaurantadvisor.ui.composable.SimpleRestaurantItem
import com.example.restaurantadvisor.ui.theme.RestaurantAdvisorTheme
import com.example.restaurantadvisor.utils.LocationManager
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

                    locationManager =
                        LocationManager(this@MainActivity, this@MainActivity) { lat, long ->
                            val isNewLocation =
                                lat != mainViewModel.location.value?.lat && long != mainViewModel.location.value?.long
                            if (isNewLocation) {
                                mainViewModel.fetchNearbyRestaurants(latLong = "$lat,$long")
                                mainViewModel.updateLocation(Location(lat, long))
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
            RestaurantRepositoryImpl(api, db)
        )
        mainViewModel = ViewModelProvider(this, mainViewModelFactory)
            .get(MainViewModel::class.java)

        val detailsViewModelFactory = DetailsViewModelFactory(
            RestaurantRepositoryImpl(api, db)
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
fun SearchTabContent(mainViewModel: MainViewModel) {
    val textInputValue = remember { mutableStateOf("") }

    val uiState by mainViewModel.uiState.collectAsState()
    val locationState by mainViewModel.location.collectAsState()

    Log.d(MainActivity.TAG, "UI state: $uiState")

    LaunchedEffect(key1 = uiState.isAutoSearchEnabled) {
        if (uiState.isAutoSearchEnabled) {
            mainViewModel.fetchNearbyRestaurants(latLong = locationState.toString())
        }
    }

    when {
        uiState.locPermissionGranted -> {
            val keyboardController = LocalSoftwareKeyboardController.current

            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.height(72.dp)) {
                    SearchBar(query = textInputValue.value,
                        onQueryChange = { textInputValue.value = it },
                        onSearch = {
                            mainViewModel.searchRestaurants(textInputValue.value)
                            keyboardController?.hide()
                        },
                        active = true,
                        placeholder = { Text(stringResource(string.search_for_restaurants)) },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = stringResource(id = R.string.search_icon_content_desc)
                            )
                        },
                        onActiveChange = {}) {

                    }
                }

                if (uiState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    uiState.foundRestaurants.forEach { restaurant ->
                        item {
                            // open the restaurant details screen on item click
                            FoundRestaurantItem(
                                name = restaurant.name,
                                address = restaurant.address,
                                isFavourite = uiState.favouriteRestaurants.any { it.id == restaurant.id },
                                distance = restaurant.distance,
                                onFavouriteClick = { isFavourite ->
                                    mainViewModel.toggleFavourite(restaurant.id, isFavourite)
                                },
                            )
                        }
                    }

                    if (!uiState.isAutoSearchEnabled) {
                        item {
                            Button(onClick = { mainViewModel.enableAutoSearch() }) {
                                Text(text = stringResource(string.enable_auto_search))
                            }
                        }
                    }
                }
            }
        }

        uiState.showRationale == false -> {
            Text(stringResource(string.location_permission_denied))
        }

        else -> {
            Button(onClick = { mainViewModel.requestLocationPermission() }) {
                Text(text = stringResource(string.request_location_permissions))
            }
        }
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

@Composable
fun MainScreen(mainViewModel: MainViewModel, detailsViewModel: DetailsViewModel, navController: NavHostController) {
    val selectedTabIndex = remember { mutableStateOf(0) }

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex.value, modifier = Modifier
        ) {
            Tab(selected = selectedTabIndex.value == 0, onClick = {
                selectedTabIndex.value = 0
            }) {
                Text("Search") // consciously hard coding string resources
            }
            Tab(selected = selectedTabIndex.value == 1, onClick = {
                selectedTabIndex.value = 1
            }) {
                Text("Favourite")
            }

        }

        when (selectedTabIndex.value) {
            0 -> SearchTabContent(mainViewModel)
            1 -> FavouriteTabContent(mainViewModel, detailsViewModel,  navController)
        }
    }
}

@Preview
@Composable
fun SearchTabContentPreview() {
    val service = RestaurantService()
    val db = Room.databaseBuilder(
        MainActivity(), AppDatabase::class.java, "database-name"
    ).build()
    val mainViewModel = MainViewModel(RestaurantRepositoryImpl(service, db))
    RestaurantAdvisorTheme {
        SearchTabContent(mainViewModel)
    }
}

@Composable
fun FavouriteTabContent(mainViewModel: MainViewModel, detailsViewModel: DetailsViewModel, navController: NavHostController) {
    val uiState by mainViewModel.uiState.collectAsState()

    Log.d(MainActivity.TAG, "UI state: $uiState")

    LazyColumn {
        uiState.favouriteRestaurants.forEach {
            item {
                SimpleRestaurantItem(id = it.id, name = it.name, address = it.address, onItemClick = {
                    detailsViewModel.fetchRestaurantDetails(it)
                    navController.navigate("restaurant_details")
                })
            }
        }
    }
}
