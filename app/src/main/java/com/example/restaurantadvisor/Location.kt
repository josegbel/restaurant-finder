package com.example.restaurantadvisor

data class Location(val lat: Double, val long: Double){
    override fun toString(): String {
        return "$lat,$long"
    }
}