package in2000.prosjekt.in2000project.ui.information

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
import in2000.prosjekt.in2000project.ui.reusables.DangerLevelItem
import in2000.prosjekt.in2000project.ui.reusables.EmergencySection
import in2000.prosjekt.in2000project.ui.reusables.TopBar
import in2000.prosjekt.in2000project.ui.reusables.ExternalResources
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
                onSettingsClick = onSettingsClick
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item { EmergencySection() }

            item {
                Text(
                    text = stringResource(R.string.danger_levels_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = Bold
                )
            }

            items(state.dangerLevels) { uiModel: DangerLevelUiModel ->
                DangerLevelItem(uiModel = uiModel)
            }

            item {
                InfoBox(
                    titleRes = R.string.other_resources_title,
                    descRes = R.string.other_resources_descr,
                    content = { ExternalResources() }
                )
            }

            item {
                InfoBox(
                    titleRes = R.string.avalanche_info_title,
                    descRes = R.string.avalanche_general_info_text
                )
            }

            item {
                InfoBox(
                    titleRes = R.string.team_title,
                    descRes = R.string.about_team_text
                )
            }
        }
    }
}

@Composable
fun InfoBox(
    modifier: Modifier = Modifier,
    titleRes: Int,
    descRes: Int,
    content: (@Composable () -> Unit)? = null
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val expandedText = stringResource(if (expanded) R.string.hide_info else R.string.show_info)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = expandedText,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(descRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    content?.invoke()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewInfoScreen() {
    InfoContent(
        state = InfoUiState(
            dangerLevels = DangerLevel.entries.map { it.toUiModel() }
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewInfoBox() {
    InfoBox(
        titleRes = R.string.team_title,
        descRes = R.string.about_team_text
    )
}