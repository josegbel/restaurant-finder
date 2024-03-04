package com.example.restaurantadvisor

interface RestaurantApi {
    fun fetchNearbyRestaurants(limit: Int, latLong: String): Result<List<Restaurant>>
}

class RestaurantApiImpl: RestaurantApi {
    override fun fetchNearbyRestaurants(limit: Int, latLong: String): Result<List<Restaurant>> {
        return Result.Success(
            listOf(
                Restaurant("1", "Restaurant 1", "123 Main St", false),
                Restaurant("2", "Restaurant 2", "456 Elm St", false),
                Restaurant("3", "Restaurant 3", "789 Oak St", false)
            )
        )
    }

}