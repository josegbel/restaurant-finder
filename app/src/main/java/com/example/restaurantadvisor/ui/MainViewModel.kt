package com.example.restaurantadvisor.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.restaurantadvisor.data.repository.RestaurantRepository
import com.example.restaurantadvisor.model.Location
import com.example.restaurantadvisor.model.Restaurant
import com.example.restaurantadvisor.ui.Error.NETWORK_ERROR
import com.example.restaurantadvisor.ui.MainViewModel.UiEvent.RequestPermission
import com.example.restaurantadvisor.utils.Result
import com.example.restaurantadvisor.utils.Result.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            _events.send(RequestPermission)
        }
    }

    fun fetchNearbyRestaurants(latLong: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true
                )
            }

            repository.fetchNearbyRestaurants(latLong).let { result ->
                when (result) {
                    is Success<List<Restaurant>> -> {
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
                                error = NETWORK_ERROR,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun searchRestaurants(query: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                )
            }

            repository.fetchRestaurantByName(query).let { result ->
                when (result) {
                    is Success<List<Restaurant>> -> {
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
                                error = NETWORK_ERROR,
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

    fun updateError(error: Error?) {
        _uiState.update { currentState ->
            currentState.copy(error = error)
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

    companion object {
        private const val TAG = "MainViewModel"
    }
}

enum class Error {
    REQUIRE_LOCATION_PERMISSION, NETWORK_ERROR
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