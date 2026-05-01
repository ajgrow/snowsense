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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.DangerLevelItem
import in2000.prosjekt.in2000project.ui.reusables.EmergencySection
import in2000.prosjekt.in2000project.ui.reusables.TopBar

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    infoViewModel: InfoViewModel = viewModel()
) {
    val uiState by infoViewModel.infoUiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                title = stringResource(R.string.about_app_title),
                modifier = Modifier,
                navController = navController
            )
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
            item { Text(
                text = stringResource(R.string.emergency_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            ) }
            item { EmergencySection() }

            item { Text(
                text = stringResource(R.string.danger_levels_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            ) }

            items(uiState.dangerLevels) { uiModel ->
                DangerLevelItem(
                    uiModel = uiModel,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            item {
                Column {

                    Text(
                        text = stringResource(R.string.avalanche_info_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = stringResource(R.string.avalanche_general_info_text),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = stringResource(R.string.team_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = stringResource(R.string.about_team_text),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}