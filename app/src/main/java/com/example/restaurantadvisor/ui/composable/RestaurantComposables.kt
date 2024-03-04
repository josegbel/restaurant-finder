package com.example.restaurantadvisor.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.restaurantadvisor.R.string
import com.example.restaurantadvisor.utils.roundOffDecimal

@Composable
fun FoundRestaurantItem(
    name: String,
    address: String,
    isFavourite: Boolean,
    distance: String?,
    onFavouriteClick: (Boolean) -> Unit,
) {
    ListItem(headlineContent = {
        Row {
            Text(text = name)
            if (distance != null) {
                val inMiles = distance.toFloat().roundOffDecimal()
                Text(
                    text = stringResource(string.miles, inMiles),
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }, supportingContent = { Text(text = address) }, trailingContent = {
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
    })
}

@Composable
fun SimpleRestaurantItem(id: String, name: String, address: String, onItemClick: (String) -> Unit) {
    ListItem(headlineContent = { Text(text = name) },
        supportingContent = { Text(text = address) },
        modifier = Modifier.clickable { onItemClick(id) }
    )
}