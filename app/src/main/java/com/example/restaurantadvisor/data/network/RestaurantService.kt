package com.example.restaurantadvisor.data.network

import com.example.restaurantadvisor.BuildConfig
import com.example.restaurantadvisor.model.Restaurant
import com.example.restaurantadvisor.utils.Result
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit

@ExperimentalSerializationApi
class RestaurantService {

    private val jsonConfig = Json {
        ignoreUnknownKeys = true // Configure Json parsing settings
    }
    private val retrofit = Retrofit.Builder().baseUrl("https://api.content.tripadvisor.com/api/v1/")
        .addConverterFactory(jsonConfig.asConverterFactory(MediaType.get("application/json")))
        .build()

    private val tripAdvisorApi = retrofit.create(TripAdvisorApi::class.java)

    fun fetchNearbyRestaurants(latLong: String): Result<List<Restaurant>> {
        return tripAdvisorApi.fetchNearbyRestaurants(latLong, BuildConfig.TRIPADVISOR_API_KEY).let {
            val response = it.execute()
            if (response.isSuccessful) {
                response.body()?.let { restaurants ->
                    Result.Success(restaurants.results.map { restaurant ->
                        Restaurant(
                            id = restaurant.locationID,
                            name = restaurant.name,
                            address = restaurant.address.fullAddress,
                            distance = restaurant.distance
                        )
                    })
                } ?: Result.Success(emptyList<Restaurant>())
            } else {
                Result.Error(Exception("Failed to fetch nearby restaurants"))
            }
        }
    }

    fun fetchRestaurantByName(query: String): Result<List<Restaurant>> {
        return tripAdvisorApi.fetchRestaurantsByQuery(query, BuildConfig.TRIPADVISOR_API_KEY).let {
            val response = it.execute()
            if (response.isSuccessful) {
                response.body()?.let { restaurants ->
                    Result.Success(restaurants.results.map { restaurant ->
                        Restaurant(
                            id = restaurant.locationID,
                            name = restaurant.name,
                            address = restaurant.address.fullAddress
                        )
                    })
                } ?: Result.Success(emptyList<Restaurant>())
            } else {
                Result.Error(Exception("Failed to fetch nearby restaurants"))
            }
        }
    }

    fun fetchRestaurantDetails(id: String): Result<Restaurant>{
        return tripAdvisorApi.fetchRestaurantDetails(id, BuildConfig.TRIPADVISOR_API_KEY).let {
            val response = it.execute()
            if (response.isSuccessful) {
                response.body()?.let { restaurant ->
                    Result.Success(
                        Restaurant(
                            id = restaurant.locationID,
                            name = restaurant.name,
                            address = restaurant.address.fullAddress,
                            distance = null,
                            phone = restaurant.phone,
                            description = restaurant.description,
                            email = restaurant.email,
                            rating = restaurant.rating,
                            website = restaurant.website,
                            isFavourite = false
                        )
                    )
                } ?: Result.Error(Exception("Failed to fetch restaurant details"))
            } else {
                Result.Error(Exception("Failed to fetch restaurant details"))
            }
        }
    }
}