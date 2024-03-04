package com.example.restaurantadvisor.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Restaurant(
    @PrimaryKey val uid: Int,
    val name: String,
    val address: String,
    val isFavourite: Boolean = false
)