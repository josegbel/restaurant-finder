package com.example.restaurantadvisor.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.restaurantadvisor.data.entities.Restaurant

@Database(entities = [Restaurant::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
}