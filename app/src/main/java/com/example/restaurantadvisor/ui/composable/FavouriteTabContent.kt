package com.example.restaurantadvisor.ui.composable

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.example.restaurantadvisor.ui.viewmodels.DetailsViewModel
import com.example.restaurantadvisor.ui.MainActivity
import com.example.restaurantadvisor.ui.viewmodels.MainViewModel

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