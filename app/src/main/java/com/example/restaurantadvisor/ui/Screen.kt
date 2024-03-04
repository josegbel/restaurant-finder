package com.example.restaurantadvisor.ui

import com.example.restaurantadvisor.Restaurant

sealed class Screen {
    object ListScreen : Screen()
    data class DetailScreen(val restaurant: Restaurant) : Screen()
}