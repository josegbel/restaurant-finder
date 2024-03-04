package com.example.restaurantadvisor.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressObj(
    @SerialName("address_string")
    val fullAddress: String = "",
    val city: String = "",
    val country: String = "",
    val postalcode: String = "",
    val state: String = "",
    val street1: String = "",
    val street2: String = ""
)