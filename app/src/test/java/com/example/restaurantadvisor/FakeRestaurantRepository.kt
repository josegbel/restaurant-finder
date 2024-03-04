package com.example.restaurantadvisor

import com.example.restaurantadvisor.data.repository.RestaurantRepository
import com.example.restaurantadvisor.model.Restaurant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.restaurantadvisor.utils.Result

class FakeRestaurantRepository(var shouldReturnNetworkError: Boolean = false) : RestaurantRepository {
    var restaurants = mutableListOf<Restaurant>(
        Restaurant(
            "1",
            "McDonald's",
            "Fast Food",
            "123 Main St",
            "123-456-7890",
            "https://www.mcdonalds.com",
            "https://www.mcdonalds.com",
            "https://www.mcdonalds.com",
            "4.5",
            false,
    ),
        Restaurant(
            "2",
            "Burger King",
            "Fast Food",
            "456 Main St",
            "123-456-7890",
            "https://www.burgerking.com",
            "https://www.burgerking.com",
            "https://www.burgerking.com",
            "4.5",
            false,
    ),
        Restaurant(
            "3",
            "Wendy's",
            "Fast Food",
            "789 Main St",
            "123-456-7890",
            "https://www.wendys.com",
            "https://www.wendys.com",
            "https://www.wendys.com",
            "4.5",
            false,
    ),
    )
    var favouriteRestaurants = mutableSetOf<Restaurant>()

    override suspend fun saveRestaurants(restaurants: List<Restaurant>) {
        this.restaurants.addAll(restaurants)
    }

    override suspend fun getRestaurants(ids: List<Int>): List<Restaurant> {
        return restaurants.filter { it.id.toInt() in ids }
    }

    override suspend fun getFavouriteRestaurants(): Flow<List<Restaurant>> = flow {
        emit(favouriteRestaurants.toList())
    }

    override suspend fun toggleFavourite(id: String, isFavourite: Boolean) {
        val restaurant = restaurants.find { it.id == id }
        restaurant?.let {
            if (isFavourite) {
                favouriteRestaurants.add(it)
            } else {
                favouriteRestaurants.remove(it)
            }
        }
    }

    override suspend fun fetchNearbyRestaurants(latLong: String): Result<List<Restaurant>> {
        return if (shouldReturnNetworkError) {
            Result.Error(Exception("Network error"))
        } else {
            Result.Success(restaurants.take(10))
        }
    }

    override suspend fun fetchRestaurantByName(query: String): Result<List<Restaurant>> {
        return if (shouldReturnNetworkError) {
            Result.Error(Exception("Network error"))
        } else {
            Result.Success(restaurants.filter { it.name.contains(query, ignoreCase = true) })
        }
    }

    override suspend fun fetchRestaurantDetails(id: String): Result<Restaurant> {
        return if (shouldReturnNetworkError) {
            Result.Error(Exception("Network error"))
        } else {
            restaurants.find { it.id == id }?.let { Result.Success(it) } ?: Result.Error(Exception("Not found"))
        }
    }
}
