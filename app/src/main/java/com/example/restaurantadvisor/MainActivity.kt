@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.restaurantadvisor

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.restaurantadvisor.Error.NETWORK_ERROR
import com.example.restaurantadvisor.Error.REQUIRE_LOCATION_PERMISSION
import com.example.restaurantadvisor.R.string
import com.example.restaurantadvisor.ui.theme.RestaurantAdvisorTheme
import kotlinx.coroutines.launch

const val TAG = "tag"

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onStart() {
        super.onStart()

        val mainViewModel: MainViewModel by viewModels()

        requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                mainViewModel.updatePermissionState(isGranted = true)
            } else {
                val shouldShowRationale =
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                mainViewModel.updatePermissionState(
                    isGranted = false, shouldShowRationale = shouldShowRationale
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = MainViewModelFactory(RestaurantRepository())
        val mainViewModel: MainViewModel by viewModels { factory }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.uiState.collect { uiState ->

                    if (!uiState.locPermissionGranted) {
                        mainViewModel.requestLocationPermission()
                    }

                    setContent {
                        RestaurantAdvisorTheme {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                when (uiState.error) {
                                    REQUIRE_LOCATION_PERMISSION -> {

                                    }

                                    NETWORK_ERROR -> TODO()

                                    null -> {
                                        // do nothing
                                    }
                                }

                                val selectedTabIndex = remember { mutableStateOf(0) }

                                Column {
                                    TabRow(
                                        selectedTabIndex = selectedTabIndex.value,
                                        modifier = Modifier
                                    ) {
                                        Tab(selected = selectedTabIndex.value == 0, onClick = {
                                            selectedTabIndex.value = 0
                                        }) {
                                            Text("Search") // consciously hard coding string resources
                                        }
                                        Tab(selected = selectedTabIndex.value == 1, onClick = {
                                            selectedTabIndex.value = 1
                                        }) {
                                            Text("Favourite")
                                        }

                                    }

                                    when (selectedTabIndex.value) {
                                        0 -> SearchTabContent(
                                            mainViewModel,
                                            modifier = Modifier.fillMaxSize(),
                                        )

                                        1 -> FavouriteTabContent()
                                    }
                                }
                            }
                        }
                    }

                    mainViewModel.events.collect { event ->
                        when (event) {
                            MainViewModel.UiEvent.RequestPermission -> {
                                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchTabContent(mainViewModel: MainViewModel, modifier: Modifier = Modifier) {
    val textInputValue = remember { mutableStateOf("") }

    val uiState by mainViewModel.uiState.collectAsState()

    when {
        uiState.locPermissionGranted -> {
            Column(modifier = modifier.fillMaxSize()) {
                SearchBar(query = textInputValue.value,
                    onQueryChange = { textInputValue.value = it },
                    onSearch = {},
                    active = true,
                    placeholder = { Text(stringResource(string.search_for_restaurants)) },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = stringResource(id = R.string.search_icon_content_desc)
                        )
                    },
                    onActiveChange = {}) {

                }

                if (uiState.isLoading) {
                    Log.d(TAG, "Loading is true")
                    LinearProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Log.d(TAG, "Loading is false")
                    HorizontalDivider()
                    Log.d(TAG, "Restaurants: ${uiState.restaurants}")
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(uiState.restaurants.size) { id ->
                            Text(text = uiState.restaurants[id].name)
                        }
                    }
                }

            }
        }

        uiState.showRationale == false -> {
            Text(stringResource(string.location_permission_denied))
        }

        else -> {
            Button(onClick = { mainViewModel.requestLocationPermission() }) {
                Text(text = stringResource(string.request_location_permissions))
            }
        }
    }
}

@Preview
@Composable
fun SearchTabContentPreview() {
    RestaurantAdvisorTheme {
        SearchTabContent(
            MainViewModel(RestaurantRepository()), Modifier.fillMaxSize()
        )
    }
}

@Composable
fun FavouriteTabContent() {
    Text("Content for Favourite")
}
