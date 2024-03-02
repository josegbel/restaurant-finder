package com.example.restaurantadvisor

import kotlinx.coroutines.delay

class RestaurantRepository {
    suspend fun fetchNearbyRestaurants(limit: Int, latLong: String): Result<List<Restaurant>> {
        delay(4000)
        return Result.Success(
            listOf(
                Restaurant("1", "Restaurant 1", "123 Main St"),
                Restaurant("2", "Restaurant 2", "456 Elm St"),
                Restaurant("3", "Restaurant 3", "789 Oak St")
            )
        )
    }

}
