package com.example.restaurantadvisor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MainViewModel"

class MainViewModel(private val repository: RestaurantRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>()
    val events = _events.receiveAsFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    init {
        getFavouriteRestaurants()
    }

    fun updateLocation(location: Location) {
        _location.value = location
    }

    fun updatePermissionState(isGranted: Boolean, shouldShowRationale: Boolean? = null) {
        _uiState.value = uiState.value.copy(
            locPermissionGranted = isGranted,
            showRationale = shouldShowRationale ?: uiState.value.showRationale
        )
    }

    fun requestLocationPermission() {
        Log.d(TAG, "Requesting location permission")
        viewModelScope.launch {
            _events.send(UiEvent.RequestPermission)
        }
    }

    fun fetchNearbyRestaurants(limit: Int = 10, latLong: String) {
        require(limit <= 10) { "Limit must be less than or equal to 10" }
        // Fetch nearby restaurants
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true
                )
            }

            repository.fetchNearbyRestaurants(limit, latLong).let { result ->
                when (result) {
                    is Result.Success<List<Restaurant>> -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                foundRestaurants = result.data,
                            )
                        }
                    }

                    is Result.Error -> {
                        Log.e(TAG, "Network error occurred")
                        _uiState.update { currentState ->
                            currentState.copy(
                                error = Error.NETWORK_ERROR,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun searchRestaurants(query: String) {
        // Search restaurants
        // change state to loading and isAutoSearchEnabled to false
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                )
            }
            // fetch restaurants
            repository.fetchRestaurantByName(query).let { result ->
                when (result) {
                    is Result.Success<List<Restaurant>> -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                isAutoSearchEnabled = false,
                                foundRestaurants = result.data,
                            )
                        }
                    }

                    is Result.Error -> {
                        Log.e(TAG, "Network error occurred")
                        _uiState.update { currentState ->
                            currentState.copy(
                                error = Error.NETWORK_ERROR,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getFavouriteRestaurants() {
        viewModelScope.launch {
            repository.getFavouriteRestaurants().let { restaurants ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            favouriteRestaurants = restaurants.first().toSet()
                        )
                }
            }
        }
    }

    fun toggleFavourite(id: String, isFavourite: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Toggling favourite for restaurant with id $id, isFavourite: $isFavourite")
                repository.toggleFavourite(id, isFavourite)
                getFavouriteRestaurants()
            }
        }
    }

    fun enableAutoSearch() {
        _uiState.update { currentState ->
            currentState.copy(
                isAutoSearchEnabled = true
            )
        }
    }

    sealed class UiEvent {
        object RequestPermission : UiEvent()
    }

    data class UiState(
        val error: Error? = null,
        val isLoading: Boolean = false,
        val isAutoSearchEnabled: Boolean = true,
        val locPermissionGranted: Boolean = false,
        val showRationale: Boolean? = null, // null means not checked yet
        val foundRestaurants: List<Restaurant> = emptyList(),
        val favouriteRestaurants: Set<Restaurant> = emptySet()
    )
}

enum class Error {
    REQUIRE_LOCATION_PERMISSION, NETWORK_ERROR
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

class MainViewModelFactory(private val repository: RestaurantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}