package in2000.prosjekt.in2000project.ui.information

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.data.model.enums.EmergencyServices
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
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
            item { EmergencyNumbersSection () }
            item {
                Column {
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

            items(uiState.dangerLevels) { uiModel ->
                DangerLevelItem(
                    uiModel = uiModel,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun DangerLevelItem(
    modifier: Modifier = Modifier,
    uiModel: DangerLevelUiModel
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = uiModel.getColor(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(uiModel.titleRes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text =
                        if (expanded)
                            stringResource(R.string.hide_info)
                        else
                            stringResource(R.string.show_info),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = stringResource(uiModel.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EmergencyNumbersSection(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Red)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.emergency_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            EmergencyServices.entries.forEach {
                Row() {
                    Text(text = stringResource(it.service))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = stringResource(it.number))
                }
            }
        }
    }
}

@Preview
@Composable
fun PrevDangerLevelItem(){
    DangerLevelItem(uiModel = DangerLevel.HIGH.toUiModel())
}

@Preview
@Composable
fun prevEmergencySection() {
    EmergencyNumbersSection()
}
