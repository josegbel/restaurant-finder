package com.example.restaurantadvisor.ui.composable

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SearchBar
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
import androidx.room.Room
import com.example.restaurantadvisor.R.string
import com.example.restaurantadvisor.data.db.AppDatabase
import com.example.restaurantadvisor.data.network.RestaurantService
import com.example.restaurantadvisor.data.repository.RestaurantRepositoryImpl
import com.example.restaurantadvisor.ui.MainActivity
import com.example.restaurantadvisor.ui.viewmodels.MainViewModel
import com.example.restaurantadvisor.ui.theme.RestaurantAdvisorTheme
import com.example.restaurantadvisor.utils.AndroidLogger
import kotlinx.coroutines.Dispatchers

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
                                Rounded.Search,
                                contentDescription = stringResource(id = string.search_icon_content_desc)
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

@Preview
@Composable
fun SearchTabContentPreview() {
    val service = RestaurantService()
    val db = Room.databaseBuilder(
        MainActivity(), AppDatabase::class.java, "database-name"
    ).build()
    val mainViewModel = MainViewModel(RestaurantRepositoryImpl(service, db, Dispatchers.IO), AndroidLogger())
    RestaurantAdvisorTheme {
        SearchTabContent(mainViewModel)
    }
}

