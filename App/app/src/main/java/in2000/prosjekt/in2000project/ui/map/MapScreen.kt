package in2000.prosjekt.in2000project.ui.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.SkiArea
import in2000.prosjekt.in2000project.data.model.toSkiArea
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.ClimateGraph
import in2000.prosjekt.in2000project.ui.reusables.DangerLevelCard
import in2000.prosjekt.in2000project.ui.reusables.DatePicker
import org.koin.compose.viewmodel.koinViewModel
import java.time.MonthDay

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.ui.search.SearchViewModel
import in2000.prosjekt.in2000project.ui.settings.SettingsViewModel
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
import in2000.prosjekt.in2000project.ui.reusables.TextInputDialog
import kotlinx.coroutines.launch
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
    navController: NavController,
    coordinates: String,
    searchViewModel: SearchViewModel,
    mapViewModel: MapViewModel = koinViewModel(),
    nveViewModel: NveViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val screenState by mapViewModel.screenState.collectAsState()
    DisposableEffect(Unit) {
        onDispose {
            mapViewModel.onSheetDismissed()
        }
    }


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
        onPeriodStart = { it ->
            if (it != null) mapViewModel.onPeriodStart(it) },
        onPeriodEnd = { it ->
            if (it != null) mapViewModel.onPeriodEnd(it) },
        onYearsBack = { it ->
            if (it != null) mapViewModel.onYearsBack(it) },
        settingsViewModel = settingsViewModel,
        searchViewModel = searchViewModel
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
    onPeriodStart: (MonthDay?) -> Unit,
    onPeriodEnd: (MonthDay?) -> Unit,
    onYearsBack: (Int?) -> Unit,
    settingsViewModel: SettingsViewModel,
    searchViewModel: SearchViewModel
) {
    val skiArea = remember(screenState.climateState) {
        val state = screenState.climateState
        if (state is ClimateState.Success) {
            state.data.toSkiArea()
        } else {
            null
        }
    } ?: previewSkiArea

    val searchState by searchViewModel.searchState.collectAsStateWithLifecycle()

    val dangerCheckUiState by nveViewModel.dangerCheckUiState.collectAsState()

    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val scope = rememberCoroutineScope()
    //Controls whether the bottom sheet is visible or not by keeping track of if the user has clicked the map
    //if this is set to null then the ModalBottomSheet is not open
    var isSheetOpen by remember { mutableStateOf(false) }

    LaunchedEffect(screenState.climateState) {
        if (screenState.climateState is ClimateState.Success) {
            isSheetOpen = true
        }
    }

    val avalancheStatus = when {
        dangerCheckUiState.isDanger == true -> "High"
        dangerCheckUiState.isDanger == false -> "Low"
        else -> "Haven't checked"
    }

    var startDate by rememberSaveable { mutableStateOf<MonthDay?>(null) }
    var endDate by rememberSaveable { mutableStateOf<MonthDay?>(null) }
    var selectedYear by rememberSaveable { mutableStateOf<Int?>(null) }

    // Animate map height fraction when sheet opens/closes
    val mapHeightFraction by animateFloatAsState(
        targetValue = if (isSheetOpen) 0.4f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "mapHeight"
    )


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

    Scaffold(
        modifier = modifier,
        bottomBar = { BottomBar(modifier, navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(mapHeightFraction)
            ) {
                MapboxView(
                    onMapClick = { point ->
                        onMapClick(point.latitude(), point.longitude())
                        nveViewModel.avalancheChecker(point.latitude(), point.longitude())
                        isSheetOpen = true
                    },
                    onOverlayError = onOverlayError,
                    onOverlayLoaded = onOverlayLoaded,
                    modifier = Modifier.fillMaxSize(),
                    coordinates = coordinates,
                    searchState = searchState,
                    onSearch = { searchViewModel.onSearch() },
                    onSelectResult = searchViewModel::addRecentSearch
                )

                when (screenState.overlayState) {
                    is OverlayState.Loading -> {
                        OverlayStatusCard(message = stringResource(R.string.overlay_loading))
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
        // Modal bottom sheet — rendered outside Scaffold so it overlays everything
        if (isSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = {
                    isSheetOpen = false
                },
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

    BottomSheetScaffold(
        modifier = Modifier.zIndex(1f),
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp, //the sheet is originally hidden by default
        sheetSwipeEnabled = true,
        sheetContent = {
            BottomSheetContent(
                area = skiArea,
                climateState = screenState.climateState,
                avalancheStatus = avalancheStatus,
                dangerLevel = dangerCheckUiState.dangerLevel,
                modifier = Modifier
                    .fillMaxWidth()
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
                        val calculatedYearsBack = Year.now().value - it
                        onYearsBack(calculatedYearsBack)
                    }
                },
                onAddFavorite = {
                    if (screenState.climateCoordinate != null) {
                        showFavoriteDialog = true
                        searchViewModel.addPendingFavorite(screenState.climateCoordinate)
                    }
                }
            )
        },

        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    //this is the reel animation - the map shrinks from full height to 40% when sheet opens
                    .fillMaxHeight(1f - (0.6f * sheetProgress)) // shrinks touch area
                    .zIndex(0f)
            )
             {
                //Map added here
                //onMapClick is passed to the Map
                MapboxView(
                    onMapClick = { point ->
                        onMapClick(point.latitude(), point.longitude())
                        nveViewModel.avalancheChecker(point.latitude(), point.longitude())
                        scope.launch { scaffoldState.bottomSheetState.expand() }
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
                 BottomBar(
                     modifier = Modifier.align(Alignment.BottomCenter),
                     navController = navController
                 )
            }
        }
    )
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



    // This is the content inside the bottom sheet.
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(rememberNestedScrollInteropConnection())
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

            items(climateRows) { row ->
                ClimateRow(
                    label = row.first,
                    value = row.second)
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
        settingsViewModel = viewModel(),
        onPeriodStart = {},
        onPeriodEnd = {},
        onYearsBack = {},
        searchViewModel = koinViewModel()
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
