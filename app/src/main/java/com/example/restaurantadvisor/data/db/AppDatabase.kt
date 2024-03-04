package com.example.restaurantadvisor.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.restaurantadvisor.data.db.entities.Restaurant

@Database(entities = [Restaurant::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
}