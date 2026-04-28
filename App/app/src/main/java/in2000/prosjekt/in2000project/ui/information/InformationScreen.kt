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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.TopBar

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    infoViewModel: InfoViewModel = viewModel()
) {

    val dangerLevels = DangerLevel.entries

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                modifier = Modifier,
                navController = navController)
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
                        text = stringResource(R.string.about_app_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
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
                        text = stringResource(R.string.danger_levels_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            //replaced the function "DangerLevelsColumn" with an "items" composable
            items(dangerLevels) { dangerLevel ->
                DangerLevelItem(
                    level = stringResource(dangerLevel.titleRes),
                    description = stringResource(dangerLevel.descriptionRes),
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
