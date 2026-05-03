package in2000.prosjekt.in2000project.ui.map

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.SkiArea
import in2000.prosjekt.in2000project.data.model.toSkiArea
import in2000.prosjekt.in2000project.ui.reusables.ClimateGraph
import in2000.prosjekt.in2000project.ui.reusables.DangerLevelCard
import in2000.prosjekt.in2000project.ui.reusables.DatePicker
import org.koin.compose.viewmodel.koinViewModel
import java.time.MonthDay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import in2000.prosjekt.in2000project.data.model.NamedLocation
import in2000.prosjekt.in2000project.ui.search.SearchViewModel
import in2000.prosjekt.in2000project.ui.settings.SettingsViewModel
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
import in2000.prosjekt.in2000project.ui.reusables.TextInputDialog
import in2000.prosjekt.in2000project.ui.search.SearchUiState
import java.time.Year

private val previewSkiArea = SkiArea(
    name = "",
    temperatureData = emptyList(),
    precipitationData = emptyList(),
    windSpeedData = emptyList(),
    snowDepthData = emptyList()
)


//for use when the VM is ready
@Composable
fun MapRoute(
    coordinates: String,
    searchViewModel: SearchViewModel,
    mapViewModel: MapViewModel = koinViewModel(),
    nveViewModel: NveViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val screenState by mapViewModel.screenState.collectAsState()
    val dangerCheckUiState by nveViewModel.dangerCheckUiState.collectAsState()
    val searchState by searchViewModel.searchState.collectAsStateWithLifecycle()

    var showFavoriteDialog by remember { mutableStateOf(false) }

    if (showFavoriteDialog) {
        TextInputDialog(
            title = stringResource(R.string.name_favorite_prompt),
            defaultValue = "",
            onConfirm = { name ->
                if (name.isNotBlank()) {
                    searchViewModel.nameFavorite(name)
                    showFavoriteDialog = false
                }
            },
            onDismiss = { showFavoriteDialog = false }
        )
    }

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose {
            mapViewModel.onSheetDismissed()
        }
    }

    MapScreen(
        screenState = screenState,
        dangerCheckUiState = dangerCheckUiState,
        searchState = searchState,
        coordinates = coordinates,
        onMapClick = { lat, lon ->
            mapViewModel.onMapClick(lat, lon)
            nveViewModel.avalancheChecker(lat, lon)
        },
        onOverlayRetry = { mapViewModel.onOverlayLoading() },
        onOverlayLoaded = { mapViewModel.onOverlayLoaded() },
        onOverlayError = { mapViewModel.onOverlayError() },
        onSheetDismiss = { mapViewModel.onSheetDismissed() },
        onStartDateChange = { if (it != null) mapViewModel.onStartDateChange(it) },
        onEndDateChange = { if (it != null) mapViewModel.onEndDateChange(it) },
        onYearChange = { if (it != null) mapViewModel.onYearChange(it) },
        onSearch = { searchViewModel.onSearch() },
        onSelectResult = { searchViewModel.addRecentSearch(it) },
        onAddFavorite = {
            screenState.climateCoordinate?.let { coordinate ->
                searchViewModel.addPendingFavorite(coordinate)
                showFavoriteDialog = true
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    screenState: MapScreenState,
    dangerCheckUiState: DangerCheckUiState,
    searchState: SearchUiState,
    coordinates: String,
    onMapClick: (Double, Double) -> Unit,
    onOverlayRetry: () -> Unit,
    onOverlayLoaded: () -> Unit,
    onOverlayError: () -> Unit,
    onSheetDismiss: () -> Unit,
    onStartDateChange: (MonthDay?) -> Unit,
    onEndDateChange: (MonthDay?) -> Unit,
    onYearChange: (Int?) -> Unit,
    onSearch: () -> Unit,
    onSelectResult: (NamedLocation) -> Unit,
    onAddFavorite: () -> Unit,
) {
    val skiArea = remember(screenState.climateState) {
        val state = screenState.climateState
        if (state is ClimateState.Success) {
            state.data.toSkiArea()
        } else {
            null
        }
    } ?: previewSkiArea


    var skipPartiallyExpanded by rememberSaveable {mutableStateOf(false)}

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    val avalancheStatus = when {
        dangerCheckUiState.isDanger == true -> "High"
        dangerCheckUiState.isDanger == false -> "Low"
        else -> "Haven't checked"
    }

    var startDate by rememberSaveable { mutableStateOf<MonthDay?>(null) }
    var endDate by rememberSaveable { mutableStateOf<MonthDay?>(null) }
    var selectedYear by rememberSaveable { mutableStateOf<Int?>(null) }

    //Main screen content
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            //Map added here
            //onMapClick is passed to the Map
            MapboxView(
                onMapClick = { point ->
                    onMapClick(point.latitude(), point.longitude())
                },
                onOverlayError = onOverlayError,
                onOverlayLoaded = onOverlayLoaded,
                modifier = Modifier
                    .fillMaxSize(),
                coordinates = coordinates,
                searchState = searchState,
                onSearch = { onSearch() },
                onSelectResult = onSelectResult
            )

            when (screenState.overlayState) {
                is OverlayState.Loading -> {
                    OverlayStatusCard(
                        message = stringResource(R.string.overlay_loading)
                    )
                }

                is OverlayState.Error -> {
                    OverlayRetryCard(
                        message = stringResource(R.string.overlay_error),
                        onRetry = onOverlayRetry
                    )
                }

                is OverlayState.Success -> Unit
            }

            when (screenState.climateState) {
                is ClimateState.Idle -> Unit

                is ClimateState.Loading -> {
                    Log.d("ClimateData", "Loading...")
                }

                is ClimateState.Error -> {
                    Log.d("ClimateData", "Error")
                }

                is ClimateState.Success -> {
                    val success = screenState.climateState
                    val climateData = success.data
                    Log.d("ClimateData", climateData.toString())
                }
            }
        }
    }

    //This is for the bottom sheet - if isSheetOpen is set to true, then it will open otherwise, it stays closed
    if (screenState.isSheetVisible){
        ModalBottomSheet(
            onDismissRequest = { onSheetDismiss() },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxHeight()
        ) {
            BottomSheetContent(
                area = skiArea,
                climateState = screenState.climateState,
                avalancheStatus = avalancheStatus,
                dangerLevel = dangerCheckUiState.dangerLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                startDate = screenState.startDate,
                endDate = screenState.endDate,
                selectedYear = screenState.selectedYear,
                onStartDateChange = { onStartDateChange(it) },
                onEndDateChange = { onEndDateChange(it) },
                onYearChange = onYearChange,
                onAddFavorite = onAddFavorite
            )
        }
    }
}

@Composable
fun BottomSheetContent(
    area: SkiArea,
    climateState: ClimateState,
    avalancheStatus: String,
    dangerLevel: DangerLevelUiModel?,
    modifier: Modifier = Modifier,
    startDate: MonthDay?,
    endDate: MonthDay?,
    selectedYear: Int?,
    onStartDateChange: (MonthDay?) -> Unit,
    onEndDateChange: (MonthDay?) -> Unit,
    onYearChange: (Int?) -> Unit,
    onAddFavorite: () -> Unit
) {

    /*
    val climateRows = listOf(
        "Avalanche status" to avalancheStatus,
    )
    */

    val climateRows = emptyList<Pair<String, String>>()


        LazyColumn(
        modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when (climateState) {
                        is ClimateState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.size(26.dp))
                        }

                        is ClimateState.Error -> {
                            Text(
                                text = stringResource(R.string.climate_error_desc),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.weather_station_desc),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        text = area.name,
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                }
                                IconButton(
                                    modifier = Modifier,
                                    onClick = {
                                        onAddFavorite()
                                    }

                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = ""
                                    )
                                }
                            }
                        }
                    }

                    DangerLevelCard(uiModel =  dangerLevel)

                }
            }

            item {
                HorizontalDivider()
            }

            item {
                DatePicker(
                    startDate = startDate,
                    endDate = endDate,
                    selectedYear = selectedYear,
                    onStartDateChange = onStartDateChange,
                    onEndDateChange = onEndDateChange,
                    onYearChange = onYearChange,
                )

                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = stringResource(R.string.date_description),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            item {
                HorizontalDivider()
            }


            //THIS IS WHERE WE CAN PUT THE GRAPH COMPONENTS
            item {
                val currentYear = Year.now().value

                val yearRange = remember(selectedYear) {
                    selectedYear?.let { it..currentYear }
                }

                if (yearRange == null ||
                    startDate == null ||
                    endDate == null
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text( text = stringResource(R.string.no_date_selected_msg))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        ClimateGraph(
                            dataPoints = area.temperatureData,
                            startDate = startDate,
                            endDate = endDate,
                            title = stringResource(R.string.mean_temp_desc)
                        )

                        HorizontalDivider()

                        ClimateGraph(
                            dataPoints = area.precipitationData,
                            startDate = startDate,
                            endDate = endDate,
                            title = stringResource(R.string.mean_precip_desc)
                        )

                        HorizontalDivider()

                        ClimateGraph(
                            dataPoints = area.windSpeedData,
                            startDate = startDate,
                            endDate = endDate,
                            title = stringResource(R.string.mean_wind_desc)
                        )

                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MapScreenErrorPreview() {
    MapScreen(
        screenState = MapScreenState(
            overlayState = OverlayState.Error,
            climateState = ClimateState.Idle,
            climateCoordinate = null,
            startDate = MonthDay.now(),
            endDate = MonthDay.now(),
            selectedYear = 1
        ),
        onMapClick = { _, _ -> },
        coordinates = "",
        onOverlayRetry = {},
        onOverlayLoaded = {},
        onOverlayError = {},
        onSheetDismiss = {},
        onStartDateChange = {},
        onEndDateChange = {},
        onYearChange = {},
        modifier = Modifier,
        dangerCheckUiState = DangerCheckUiState(),
        searchState = SearchUiState(),
        onSearch = {},
        onSelectResult = {},
        onAddFavorite = {},
    )
}


@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
    BottomSheetContent(
        area = previewSkiArea,
        climateState = ClimateState.Idle,
        avalancheStatus = "High",
        dangerLevel = DangerLevel.HIGH.toUiModel(),
        startDate = null,
        endDate = null,
        selectedYear = null,
        onStartDateChange = { },
        onEndDateChange = { },
        onYearChange = { },
        onAddFavorite = { }
    )
}
