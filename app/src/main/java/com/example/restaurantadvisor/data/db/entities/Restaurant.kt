package com.example.restaurantadvisor.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Restaurant(
    @PrimaryKey val uid: Int,
    val name: String,
    val address: String,
    val distance: String? = null,
    val phone: String? = null,
    val description: String? = null,
    val email: String? = null,
    val rating: String? = null,
    val website: String? = null,
    val isFavourite: Boolean = false
)