package com.example.restaurantadvisor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.restaurantadvisor.ui.composable.SearchTabContent
import com.example.restaurantadvisor.ui.viewmodels.MainViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyComposeUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenLocationPermissionIsGranted_checkSearchBarIsDisplayed() {
        val mainViewModel = MainViewModel(
            FakeRestaurantRepository(),
            FakeLogger()
        )
        mainViewModel.updatePermissionState(true)
        // Set up the UI content that you want to test
        composeTestRule.setContent {
            SearchTabContent(
                mainViewModel = mainViewModel
            )
        }

        // Find the button by text and assert it is displayed
        composeTestRule.onNodeWithTag("searchBarTag").assertIsDisplayed()
    }

    @Test
    fun whenLocationPermissionIsNotGranted_checkSearchBarIsNotDisplayed() {
        val mainViewModel = MainViewModel(
            FakeRestaurantRepository(),
            FakeLogger()
        )

        mainViewModel.updatePermissionState(false)

        // Set up the UI content that you want to test
        composeTestRule.setContent {
            SearchTabContent(
                mainViewModel = mainViewModel
            )
        }

        // Find the button by text and assert it is displayed
        composeTestRule.onNodeWithTag("searchBarTag").assertIsNotDisplayed()
    }
}
