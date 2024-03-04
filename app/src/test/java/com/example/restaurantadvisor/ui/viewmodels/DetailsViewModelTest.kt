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

class DetailsViewModelTest {

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
    fun `Given valid restaurant id, when fetchRestaurantDetails is called, then uiState updates with restaurant details`() = runTest {

        // Given
        val fakeRepository = FakeRestaurantRepository()
        val viewModel = DetailsViewModel(fakeRepository, FakeLogger())

        // When
        viewModel.fetchRestaurantDetails("1")

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.error == null && uiState.restaurant != null && !uiState.isLoading)
    }

    @Test
    fun `Given network error, when fetchRestaurantDetails is called, then uiState updates with network error`() {
        // Given
        val fakeRepository = FakeRestaurantRepository(shouldReturnNetworkError = true)
        val viewModel = DetailsViewModel(fakeRepository, FakeLogger())

        // When
        viewModel.fetchRestaurantDetails("1")

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.error == DetailsError.NETWORK_ERROR && uiState.restaurant == null && !uiState.isLoading)
    }
}