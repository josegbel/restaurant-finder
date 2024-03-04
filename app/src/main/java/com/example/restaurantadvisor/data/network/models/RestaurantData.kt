package com.example.restaurantadvisor.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RestaurantData(
    @SerialName("address_obj")
    val address: AddressObj,
    val bearing: String = "",
    val distance: String = "",
    @SerialName("location_id")
    val locationID: String = "",
    val rating: String = "",
    val phone: String = "",
    val email: String = "",
    val description: String = "",
    val website: String = "",
    val name: String = ""
)