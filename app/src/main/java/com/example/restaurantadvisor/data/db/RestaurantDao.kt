package com.example.restaurantadvisor.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.restaurantadvisor.data.db.entities.Restaurant
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurant WHERE isFavourite = 1")
    fun getAllFavorites(): Flow<List<Restaurant>>

    @Query("SELECT uid FROM restaurant")
    fun getAllIds(): List<Int>

    @Query("UPDATE restaurant SET isFavourite = :isFavourite WHERE uid = :id")
    fun toggleFavourite(id: Int, isFavourite: Boolean)

    @Query("SELECT * FROM restaurant WHERE uid IN (:restaurantIds)")
    fun loadAllByIds(restaurantIds: IntArray): List<Restaurant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(restaurants: List<Restaurant>)
}