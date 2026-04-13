package in2000.prosjekt.in2000project.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.TopBar
import in2000.prosjekt.in2000project.ui.map.MapScreen
import in2000.prosjekt.in2000project.ui.map.MapUIState

@Composable
fun MapRetryScreen (modifier: Modifier = Modifier, navController: NavController) {
    Scaffold(
        topBar = {
            TopBar(modifier = Modifier, title = "MapScreen")
        },
        bottomBar = {
            BottomBar(modifier, navController)
        }
    ) { paddingValues ->
        // Main screen content
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Makes sure the content is not hidden behind the top app bar or bottom app bar.
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Map",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Instructions
            Text(
                text = "Select an area to view historical climate details.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button (
                onClick = onRetry
            ) {
                Text(text = "Retry")
            }
            )

        }
    }
}

@Preview
@Composable
fun MapRetryScreenPreview() {
    MapRetryScreen(navController = rememberNavController())
}