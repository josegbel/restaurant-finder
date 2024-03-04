package com.example.restaurantadvisor.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.restaurantadvisor.R.string

@Composable
fun FoundRestaurantItem(name:String, address:String, isFavourite:Boolean, onFavouriteClick: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(text = name) },
        supportingContent = { Text(text = address) },
        trailingContent = {
            if (isFavourite) {
                Box(modifier = Modifier.clickable { onFavouriteClick(false) }) {
                    Icon(
                        Icons.Rounded.Favorite,
                        contentDescription = stringResource(string.remove_as_favourite)
                    )
                }
            } else {
                Box(modifier = Modifier.clickable { onFavouriteClick(true) }) {
                    Icon(
                        Icons.Rounded.FavoriteBorder,
                        contentDescription = stringResource(string.mark_as_favourite)
                    )
                }
            }
        }
    )
}

@Composable
fun SimpleRestaurantItem(name:String, address:String, isFavourite:Boolean = false) {
    ListItem(
        headlineContent = { Text(text = name) },
        supportingContent = { Text(text = address) },
        trailingContent = {
            if (isFavourite) {
                Icon(
                    Icons.Rounded.Favorite,
                    contentDescription = stringResource(string.remove_as_favourite)
                )
            } else {
                Icon(
                    Icons.Rounded.FavoriteBorder,
                    contentDescription = stringResource(string.mark_as_favourite)
                )
            }
        }
    )
}