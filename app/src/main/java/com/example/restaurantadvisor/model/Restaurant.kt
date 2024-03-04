package com.example.restaurantadvisor.model

import android.media.Rating

data class Restaurant(
    val id: String,
    val name: String,
    val address: String,
    val distance: String? = null,
    val phone: String? = null,
    val description: String? = null,
    val email: String? = null,
    val rating: String? = null,
    val website: String? = null,
    val isFavourite: Boolean = false
) {
    override fun toString(): String {
        return "$name, $address, ${if (isFavourite) "Favourite" else "Not favourite"}"
    }
}