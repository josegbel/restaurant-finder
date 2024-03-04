import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.restaurantadvisor.ui.viewmodels.DetailsViewModel

@Composable
fun RestaurantDetailsScreen(detailsViewModel: DetailsViewModel, navController: NavController) {
    val uiState by detailsViewModel.uiState.collectAsState()

    val restaurant = uiState.restaurant
    if (uiState.isLoading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = restaurant?.name ?: "",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider()
            IconRow(icon = Icons.Filled.Phone, text = restaurant?.phone ?: "")
            IconRow(icon = Icons.Filled.Email, text = restaurant?.email ?: "")
            Text(
                text = "Description",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = restaurant?.description ?: "",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = restaurant?.website ?: "",
                style = MaterialTheme.typography.bodySmall
            )
            IconRow(icon = Icons.Filled.Star, text = "Rating: ${restaurant?.rating ?: ""}")
        }
    }
}

@Composable
fun IconRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

