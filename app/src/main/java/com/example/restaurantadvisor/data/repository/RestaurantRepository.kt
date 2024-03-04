package com.example.restaurantadvisor.data.repository

import com.example.restaurantadvisor.data.db.AppDatabase
import com.example.restaurantadvisor.model.Restaurant
import com.example.restaurantadvisor.data.network.RestaurantService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.example.restaurantadvisor.data.db.entities.Restaurant as RestaurantEntity
import com.example.restaurantadvisor.utils.Result

interface RestaurantRepository {
    suspend fun saveRestaurants(restaurants: List<Restaurant>)
    suspend fun getRestaurants(ids: List<Int>): List<Restaurant>
    suspend fun getFavouriteRestaurants(): Flow<List<Restaurant>>
    suspend fun toggleFavourite(id: String, isFavourite: Boolean)

    suspend fun fetchNearbyRestaurants(latLong: String): Result<List<Restaurant>>
    suspend fun fetchRestaurantByName(query: String): Result<List<Restaurant>>
    suspend fun fetchRestaurantDetails(id: String): Result<Restaurant>
}

class RestaurantRepositoryImpl(
    private val service: RestaurantService, private val db: AppDatabase
) : RestaurantRepository {

    override suspend fun fetchNearbyRestaurants(latLong: String): Result<List<Restaurant>> {
        return withContext(Dispatchers.IO) {
            getFavouriteRestaurants().let { favouriteRestaurants ->
                service.fetchNearbyRestaurants(latLong).let { result ->
                    when (result) {
                        is Result.Success<List<Restaurant>> -> {
                            val newRestaurants = result.data.map {
                                it.copy(isFavourite = favouriteRestaurants.first().contains(it))
                            }
                            val existingIds = db.restaurantDao().getAllIds()
                            newRestaurants.filterNot { restaurant ->
                                existingIds.contains(restaurant.id.toInt())
                            }.let {
                                saveRestaurants(it)
                            }
                            Result.Success(newRestaurants.sortedBy { it.distance?.toFloatOrNull() })
                        }

                        is Result.Error -> result
                    }
                }
            }
        }
    }

    override suspend fun fetchRestaurantByName(query: String): Result<List<Restaurant>> {
        return withContext(Dispatchers.IO) {
            getFavouriteRestaurants().let { favouriteRestaurants ->
                service.fetchRestaurantByName(query).let { result ->
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
                            Result.Success(newRestaurants.sortedBy { it.name })
                        }

                        is Result.Error -> result
                    }
                }
            }
        }
    }

    override suspend fun fetchRestaurantDetails(id: String): Result<Restaurant> {
        return withContext(Dispatchers.IO) {
            service.fetchRestaurantDetails(id)
        }
    }

    override suspend fun saveRestaurants(restaurants: List<Restaurant>) {
        db.restaurantDao().insertAll(
            restaurants.map { RestaurantEntity(it.id.toInt(), it.name, it.address) }
        )
    }

    override suspend fun getRestaurants(ids: List<Int>): List<Restaurant> {
        return db.restaurantDao().loadAllByIds(ids.toIntArray()).map {
            Restaurant(it.uid.toString(), it.name, it.address, it.distance,it.phone, it.description,it.email,it.rating, it.website, it.isFavourite)
        }
    }

    override suspend fun getFavouriteRestaurants(): Flow<List<Restaurant>> {
        return withContext(Dispatchers.IO) {
            db.restaurantDao().getAllFavorites().map { restaurants ->
                restaurants.map { restaurant ->
                    Restaurant(
                        restaurant.uid.toString(),
                        restaurant.name,
                        restaurant.address,
                        restaurant.distance,
                        restaurant.phone,
                        restaurant.description,
                        restaurant.email,
                        restaurant.rating,
                        restaurant.website,
                        restaurant.isFavourite
                    )
                }
            }
        }
    }

    override suspend fun toggleFavourite(id: String, isFavourite: Boolean) {
        db.restaurantDao().toggleFavourite(id.toInt(), isFavourite)
    }
}
