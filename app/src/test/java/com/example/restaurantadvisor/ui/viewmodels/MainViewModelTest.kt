package com.example.restaurantadvisor.ui.viewmodels

import com.example.restaurantadvisor.FakeLogger
import com.example.restaurantadvisor.FakeRestaurantRepository
import com.example.restaurantadvisor.data.repository.RestaurantRepository
import com.example.restaurantadvisor.model.Restaurant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MainViewModelTest {
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `Given valid location, when fetchNearbyRestaurants is called, then uiState updates with restaurants list`() {
        // Given
        val fakeRepository = FakeRestaurantRepository()
        val viewModel = MainViewModel(fakeRepository, FakeLogger())

        // When
        viewModel.fetchNearbyRestaurants("Location A")

        // Then
        val uiState = viewModel.uiState.value
        assert(uiState.isLoading.not())
        assert(uiState.foundRestaurants.size == 3)
        assert(uiState.error == null)
    }

    @Test
    fun `Given network error, when fetchNearbyRestaurants is called, then uiState updates with network error`() {
        // Given
        val fakeRepository = FakeRestaurantRepository().apply {
            shouldReturnNetworkError = true
        }
        val viewModel = MainViewModel(fakeRepository, FakeLogger())

        // When
        viewModel.fetchNearbyRestaurants("Location A")

        // Then
        val uiState = viewModel.uiState.value
        assert(uiState.isLoading.not())
        assert(uiState.error == Error.NETWORK_ERROR)
    }

    @Test
    fun `Given a restaurant id, when toggleFavourite is called, then favouriteRestaurants updates accordingly`() = runTest(testDispatcher) {
        // Given
        val fakeRepository = FakeRestaurantRepository()
        val viewModel = MainViewModel(fakeRepository, FakeLogger())

        // Initially empty favorites
        assertTrue(viewModel.uiState.value.favouriteRestaurants.isEmpty())

        // When
        viewModel.toggleFavourite("1", true)

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.favouriteRestaurants.any { it.id == "1" })
    }
}