package com.example.restaurantadvisor.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetchRestaurantsResponse(
    @SerialName("data")
    val results: List<RestaurantData>
)