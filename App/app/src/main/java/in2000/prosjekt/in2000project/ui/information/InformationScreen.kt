package in2000.prosjekt.in2000project.ui.information

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.TopBar

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    infoViewModel: InfoViewModel = viewModel()
) {
    val uiState by infoViewModel.infoState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(modifier = Modifier, title = "Information")
        },
        bottomBar = {
            BottomBar(modifier, navController)
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = uiState.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = uiState.teamTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = uiState.aboutTeam,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = uiState.avalancheTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = uiState.avalancheGeneralInfo,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = uiState.dangerTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            //replaced the function "DangerLevelsColumn" with a "items" composable
            items(uiState.dangerLevels) { dangerLevel ->
                DangerLevelItem(
                    level = dangerLevel.level,
                    description = dangerLevel.description,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun DangerLevelItem(
    level: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = level,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall
        )
    }
}