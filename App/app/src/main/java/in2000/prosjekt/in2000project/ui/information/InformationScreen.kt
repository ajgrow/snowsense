package in2000.prosjekt.in2000project.ui.information

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
import in2000.prosjekt.in2000project.ui.reusables.DangerLevelHeader
import in2000.prosjekt.in2000project.ui.reusables.DangerLevelItem
import in2000.prosjekt.in2000project.ui.reusables.EmergencySection
import in2000.prosjekt.in2000project.ui.reusables.TopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    infoViewModel: InfoViewModel = koinViewModel()
) {
    val uiState by infoViewModel.infoUiState.collectAsState()

    InfoContent(
        modifier = modifier,
        state = uiState,
        onSettingsClick = onSettingsClick
    )
}

@Composable
fun InfoContent(
    modifier: Modifier = Modifier,
    state: InfoUiState,
    onSettingsClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                title = stringResource(R.string.information_title),
                modifier = Modifier,
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { EmergencySection() }

            item { Text(
                text = stringResource(R.string.danger_levels_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            ) }

            items(state.dangerLevels) { uiModel: DangerLevelUiModel ->
                DangerLevelItem(
                    uiModel = uiModel,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            item {
                Column {
                    InfoBox(
                        titleRes = R.string.avalanche_info_title,
                        descRes = R.string.avalanche_general_info_text
                    )
                    InfoBox(
                        titleRes = R.string.team_title,
                        descRes = R.string.about_team_text
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBox(
    modifier: Modifier = Modifier,
    titleRes: Int,
    descRes: Int
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) { Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = if (expanded)
                        stringResource(R.string.hide_info)
                    else
                        stringResource(R.string.show_info),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            AnimatedVisibility(visible = expanded) {
                Text(
                    text = stringResource(descRes),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
        }
    }        }
}
@Preview
@Composable
fun PreviewInfoScreen(){
    // Use InfoContent in Preview to avoid Koin dependency from InfoScreen
    InfoContent(
        state = InfoUiState(dangerLevels = DangerLevel.entries.map { it.toUiModel() })
    )
}

@Preview
@Composable
fun PrevInfoBox(){
    InfoBox(
        titleRes = R.string.team_title,
        descRes = R.string.about_team_text
    )
}
