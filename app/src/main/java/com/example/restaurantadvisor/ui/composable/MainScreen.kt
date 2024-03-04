package com.example.restaurantadvisor.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.restaurantadvisor.ui.viewmodels.DetailsViewModel
import com.example.restaurantadvisor.ui.viewmodels.MainViewModel

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
