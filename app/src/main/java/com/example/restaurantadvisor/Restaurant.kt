package com.example.restaurantadvisor

data class Restaurant(val id: String, val name: String, val address: String, val isFavourite: Boolean) {
    override fun toString(): String {
        return "$name, $address, ${if (isFavourite) "Favourite" else "Not favourite"}"
    }
}
