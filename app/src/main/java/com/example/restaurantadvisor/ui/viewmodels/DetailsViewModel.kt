package com.example.restaurantadvisor.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.restaurantadvisor.data.repository.RestaurantRepository
import com.example.restaurantadvisor.model.Restaurant
import com.example.restaurantadvisor.ui.viewmodels.DetailsError.NETWORK_ERROR
import com.example.restaurantadvisor.utils.AndroidLogger
import com.example.restaurantadvisor.utils.Logger
import com.example.restaurantadvisor.utils.Result
import com.example.restaurantadvisor.utils.Result.Success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(private val repository: RestaurantRepository, private val logger: Logger) :
    ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun fetchRestaurantDetails(id: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true
                )
            }

            repository.fetchRestaurantDetails(id).let { result ->
                when (result) {
                    is Result.Error -> {
                        logger.e(TAG, "Network error occurred")
                        _uiState.update { currentState ->
                            currentState.copy(
                                error = NETWORK_ERROR, isLoading = false
                            )
                        }
                    }

                    is Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                restaurant = result.data,
                            )
                        }
                    }
                }
            }
        }
    }

    data class UiState(
        val error: DetailsError? = null,
        val isLoading: Boolean = false,
        val restaurant: Restaurant? = null,
    )

    companion object {
        private const val TAG = "DetailsViewModel"
    }
}

enum class DetailsError {
    NETWORK_ERROR
}

class DetailsViewModelFactory(private val repository: RestaurantRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return DetailsViewModel(repository, AndroidLogger()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}