package in2000.prosjekt.in2000project.ui.information

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.TopBar

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    Scaffold(
        topBar = {
            TopBar(modifier = Modifier, title = "InformationScreen")
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
                text = "About the app",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Instructions
            Text(
                text = "**WRITE TEXT HERE**",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

        }
    }

}