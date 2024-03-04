package com.example.restaurantadvisor.data.network

import com.example.restaurantadvisor.data.network.models.FetchRestaurantsResponse
import com.example.restaurantadvisor.data.network.models.RestaurantData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TripAdvisorApi {
    @GET("location/nearby_search")
    fun fetchNearbyRestaurants(
        @Query("latLong") latLong: String,
        @Query("key") key: String,
        @Query("category") category: String = "restaurants"
    ): Call<FetchRestaurantsResponse>

    @GET("location/search")
    fun fetchRestaurantsByQuery(
        @Query("searchQuery") query: String,
        @Query("key") key: String,
        @Query("category") category: String = "restaurants"
    ): Call<FetchRestaurantsResponse>

    @GET("location/{id}/details")
    fun fetchRestaurantDetails(
        @Path("id") id: String,
        @Query("key") key: String,
    ): Call<RestaurantData>
}
