package com.example.restaurantadvisor

import android.util.Log
import com.example.restaurantadvisor.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface RestaurantRepository {
    suspend fun saveRestaurants(restaurants: List<Restaurant>)
    suspend fun getRestaurants(ids: List<Int>): List<Restaurant>
    suspend fun getFavouriteRestaurants(): Flow<List<Restaurant>>
    suspend fun toggleFavourite(id: String, isFavourite: Boolean)

    suspend fun fetchNearbyRestaurants(limit: Int, latLong: String): Result<List<Restaurant>>
    suspend fun fetchRestaurantByName(query: String): Result<List<Restaurant>>
}

class RestaurantRepositoryImpl(private val api: RestaurantApi, private val db: AppDatabase) :
    RestaurantRepository {
    override suspend fun fetchNearbyRestaurants(
        limit: Int, latLong: String
    ): Result<List<Restaurant>> {
        return withContext(Dispatchers.IO) {
            delay(4000)

            getFavouriteRestaurants().let { favouriteRestaurants ->
                api.fetchNearbyRestaurants(limit, latLong).let { result ->
                    when (result) {
                        is Result.Success -> {
                            val newRestaurants = result.data.map {
                                it.copy(isFavourite = favouriteRestaurants.first().contains(it))
                            }
                            val existingIds = db.restaurantDao().getAllIds()
                            newRestaurants.filterNot { restaurant ->
                                existingIds.contains(restaurant.id.toInt())
                            }.let {
                                saveRestaurants(it)
                            }
                            Result.Success(newRestaurants)
                        }

                        is Result.Error -> result
                    }
                }
            }

            val newRestaurants = listOf(
                Restaurant("1", "Restaurant 1", "123 Main St", false),
                Restaurant("2", "Restaurant 2", "456 Elm St", false),
                Restaurant("3", "Restaurant 3", "789 Oak St", false)
            )

            val existingIds = db.restaurantDao().getAllIds()
            newRestaurants.filterNot { restaurant ->
                existingIds.contains(restaurant.id.toInt())
            }.let { saveRestaurants(it) }

            Result.Success(newRestaurants)
        }
    }

    override suspend fun fetchRestaurantByName(query: String): Result<List<Restaurant>> {
        delay(4000)

        val newRestaurants = listOf(
            Restaurant("1", "Restaurant 1", "123 Main St", false),
            Restaurant("2", "Restaurant 2", "456 Elm St", false),
            Restaurant("3", "Restaurant 3", "789 Oak St", false),
            Restaurant("4", "Restaurant 4", "101 Pine St", false),
            Restaurant("5", "Restaurant 5", "202 Maple St", false),
            Restaurant("6", "Restaurant 6", "303 Cedar St", false),
        )

        val existingIds = db.restaurantDao().getAllIds()
        newRestaurants.filterNot { restaurant ->
            existingIds.contains(restaurant.id.toInt())
        }.let { saveRestaurants(it) }

        return Result.Success(newRestaurants)
    }

    override suspend fun saveRestaurants(restaurants: List<Restaurant>) {
        db.restaurantDao().insertAll(*restaurants.map {
            com.example.restaurantadvisor.data.entities.Restaurant(
                it.id.toInt(), it.name, it.address
            )
        }.toTypedArray())
    }

    override suspend fun getRestaurants(ids: List<Int>): List<Restaurant> {
        return db.restaurantDao().loadAllByIds(ids.toIntArray()).map {
            Restaurant(it.uid.toString(), it.name, it.address, it.isFavourite)
        }
    }

    override suspend fun getFavouriteRestaurants(): Flow<List<Restaurant>> {
        return withContext(Dispatchers.IO) {
            db.restaurantDao().getAllFavorites().map { restaurants ->
                restaurants.map { restaurant ->
                    Restaurant(restaurant.uid.toString(), restaurant.name, restaurant.address, restaurant.isFavourite)
                }
            }
        }
    }

    override suspend fun toggleFavourite(id: String, isFavourite: Boolean) {
        db.restaurantDao().toggleFavourite(id.toInt(), isFavourite)
    }
}
