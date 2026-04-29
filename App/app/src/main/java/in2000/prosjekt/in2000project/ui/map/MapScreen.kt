package in2000.prosjekt.in2000project.ui.map

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.DangerLevel
import in2000.prosjekt.in2000project.data.model.SkiArea
import in2000.prosjekt.in2000project.data.model.toSkiArea
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.ClimateGraph
import in2000.prosjekt.in2000project.ui.reusables.DangerLevelCard
import in2000.prosjekt.in2000project.ui.reusables.DatePicker
import in2000.prosjekt.in2000project.ui.reusables.TopBar
import in2000.prosjekt.in2000project.ui.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.time.MonthDay

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
    navController: NavController,
    coordinates: String,
    mapViewModel: MapViewModel = koinViewModel(),
    nveViewModel: NveViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val screenState by mapViewModel.screenState.collectAsState()

    MapScreen(
        navController = navController,
        screenState = screenState,
        nveViewModel = nveViewModel,
        onMapClick = { lat, lon ->
            mapViewModel.onMapClick(lat, lon)
            nveViewModel.avalancheChecker(lat, lon)
        },
        coordinates = coordinates,
        onOverlayRetry = { mapViewModel.onOverlayLoading() },
        onOverlayLoaded = { mapViewModel.onOverlayLoaded()},
        onOverlayError = { mapViewModel.onOverlayError()},
        onSheetDismiss = { mapViewModel.onSheetDismissed() },
        onPeriodStart = { it ->
            if (it != null) mapViewModel.onPeriodStart(it) },
        onPeriodEnd = { it ->
            if (it != null) mapViewModel.onPeriodEnd(it) },
        onYearsBack = { it ->
            if (it != null) mapViewModel.onYearsBack(it) },
        settingsViewModel = settingsViewModel
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    screenState: MapScreenState,
    nveViewModel: NveViewModel,
    onMapClick: (Double, Double) -> Unit,
    coordinates: String,
    onOverlayRetry: () -> Unit,
    onOverlayLoaded: () -> Unit,
    onOverlayError: () -> Unit,
    onSheetDismiss: () -> Unit,
    onPeriodStart: (MonthDay?) -> Unit,
    onPeriodEnd: (MonthDay?) -> Unit,
    onYearsBack: (Int?) -> Unit,
    settingsViewModel: SettingsViewModel
) {
    val skiArea = remember(screenState.climateState) {
        val state = screenState.climateState
        if (state is ClimateState.Success) {
            state.data.toSkiArea()
        } else {
            null
        }
    } ?: previewSkiArea

    val dangerCheckUiState by nveViewModel.dangerCheckUiState.collectAsState()
    //Controls whether the bottom sheet is visible or not by keeping track of if the user has clicked the map
    //if this is set to null then the ModalBottomSheet is not open
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(screenState.climateState) {
        if (screenState.climateState is ClimateState.Success) {
            isSheetOpen = true
        }
    }

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

        //Main screen content
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Makes sure the content is not hidden behind the top app bar or bottom app bar.
                .padding(paddingValues)
                .padding(16.dp)
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
                        nveViewModel.avalancheChecker(point.latitude(), point.longitude())
                        isSheetOpen = true
                    },
                    onOverlayError = onOverlayError,
                    onOverlayLoaded = onOverlayLoaded,
                    modifier = Modifier
                        .fillMaxSize(),
                    coordinates = coordinates,
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
    }

    //This is for the bottom sheet - if isSheetOpen is set to true, then it will open otherwise, it stays closed
    if (isSheetOpen){
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxHeight()
        ) {
            BottomSheetContent(
                area = skiArea,
                climateState = screenState.climateState,
                avalancheStatus = avalancheStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                startDate = startDate,
                endDate = endDate,
                selectedYear = selectedYear,
                onStartDateChange = {
                    startDate = it
                    onPeriodStart(it)
                },
                onEndDateChange = {
                    endDate = it
                    onPeriodEnd(it)
                },
                onYearChange = { year ->
                    selectedYear = year
                    year?.let {
                        val calculatedYearsBack = java.time.Year.now().value - it
                        onYearsBack(calculatedYearsBack)
                    }
                }
            )
        }
    }
}

@Composable
fun BottomSheetContent(
    area: SkiArea,
    climateState: ClimateState,
    avalancheStatus: String,
    modifier: Modifier = Modifier,
    startDate: MonthDay?,
    endDate: MonthDay?,
    selectedYear: Int?,
    onStartDateChange: (MonthDay?) -> Unit,
    onEndDateChange: (MonthDay?) -> Unit,
    onYearChange: (Int?) -> Unit,
) {

    /*
    val climateRows = listOf(
        "Avalanche status" to avalancheStatus,
    )
    */

    val climateRows = emptyList<Pair<String, String>>()


    // This is the content inside the bottom sheet.
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
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
                        }
                    }

                    DangerLevelCard(dangerLevel = DangerLevel.VERY_HIGH, backgroundColor = Color.Red)
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

            items(climateRows) { row ->
                ClimateRow(
                    label = row.first,
                    value = row.second)
            }

            //THIS IS WHERE WE CAN PUT THE GRAPH COMPONENTS
            item {
                val currentYear = java.time.Year.now().value

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

@Composable
fun ClimateRow(
    label: String,
    value: String
) {
    // This creates the rows of information within the bottom sheet i.e. Average snowfall, Average rainfall etc.
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview
@Composable
fun MapScreenErrorPreview() {
    MapScreen(
        navController = rememberNavController(),
        screenState = MapScreenState(
            overlayState = OverlayState.Error,
            climateState = ClimateState.Idle,
            climateCoordinate = null,
            periodStart = MonthDay.now(),
            periodEnd = MonthDay.now(),
            yearsBack = 1
        ),
        nveViewModel = viewModel(),
        onMapClick = { _, _ -> },
        coordinates = "",
        onOverlayRetry = {},
        onOverlayLoaded = {},
        onOverlayError = {},
        onSheetDismiss = {},
        settingsViewModel = viewModel(),
        onPeriodStart = {},
        onPeriodEnd = {},
        onYearsBack = {},
    )
}


@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
    BottomSheetContent(
        area = previewSkiArea,
        climateState = ClimateState.Idle,
        avalancheStatus = "High",
        startDate = null,
        endDate = null,
        selectedYear = null,
        onStartDateChange = { },
        onEndDateChange = { },
        onYearChange = { },
    )
}
