package com.example.restaurantadvisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class MainViewModelFactory(private val repository: RestaurantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainViewModel(private val repository: RestaurantRepository) : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>()
    val events = _events.receiveAsFlow()

    fun updatePermissionState(isGranted: Boolean) {
        _uiState.value = uiState.value.copy(locPermissionGranted = isGranted)
    }

    fun requestLocationPermission() {
        viewModelScope.launch {
            _events.send(UiEvent.RequestPermission)
        }
    }

    // Handle business logic
    fun showError(err: Error) {
        _uiState.update { currentState ->
            currentState.copy(
                error = err
            )
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
                                restaurants = result.data
                            )
                        }
                    }

                    is Result.Error -> {
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

    }

    fun onLocationPermissionGranted(latLong: String) {
        fetchNearbyRestaurants(latLong= "123, 456")
    }

    sealed class UiEvent {
        object RequestPermission : UiEvent()
    }

    data class UiState(
        val error: Error? = null,
        val isLoading: Boolean = false,
        val locPermissionGranted: Boolean = false,
        val restaurants: List<Restaurant> = emptyList()
    )
}

enum class Error {
    REQUIRE_LOCATION_PERMISSION, NETWORK_ERROR
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}