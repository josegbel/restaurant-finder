package com.example.restaurantadvisor.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.restaurantadvisor.data.entities.Restaurant
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurant")
    fun getAll(): Flow<Restaurant?>

    @Query("SELECT * FROM restaurant WHERE isFavourite = 1")
    fun getAllFavorites(): Flow<List<Restaurant>>

    @Query("SELECT uid FROM restaurant")
    fun getAllIds(): List<Int>

    @Query("UPDATE restaurant SET isFavourite = :isFavourite WHERE uid = :id")
    fun toggleFavourite(id: Int, isFavourite: Boolean)

    @Query("SELECT * FROM restaurant WHERE uid IN (:restaurantIds)")
    fun loadAllByIds(restaurantIds: IntArray): List<Restaurant>

    @Query("SELECT * FROM restaurant WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): Restaurant

    @Insert
    fun insertAll(vararg restaurants: Restaurant)

    @Delete
    fun delete(restaurant: Restaurant)
}