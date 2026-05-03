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
import in2000.prosjekt.in2000project.data.model.NamedLocation
import in2000.prosjekt.in2000project.ui.search.SearchViewModel
import in2000.prosjekt.in2000project.ui.settings.SettingsViewModel
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
import in2000.prosjekt.in2000project.ui.reusables.TextInputDialog
import kotlinx.coroutines.launch
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
    navController: NavController,
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

    DisposableEffect(Unit) {
        onDispose {
            mapViewModel.onSheetDismissed()
        }
    }

    MapScreen(
        navController = navController,
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
        },
        canAddFavorite = searchViewModel::canAddFavorite
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
    onStartDateChange: (MonthDay?) -> Unit,
    onEndDateChange: (MonthDay?) -> Unit,
    onYearChange: (Int?) -> Unit,
    onSearch: () -> Unit,
    onSelectResult: (NamedLocation) -> Unit,
    onAddFavorite: () -> Unit,
    canAddFavorite: () -> Boolean
) {
    val skiArea = remember(screenState.climateState) {
        val state = screenState.climateState
        if (state is ClimateState.Success) {
            state.data.toSkiArea()
        } else {
            null
        }
    } ?: previewSkiArea

    val scope = rememberCoroutineScope()

    //Controls whether the bottom sheet is visible or not by keeping track of if the user has clicked the map
    //if this is set to null then the ModalBottomSheet is not open
    var isSheetOpen by remember { mutableStateOf(false) }

    LaunchedEffect(screenState.climateState) {
        if (screenState.climateState is ClimateState.Success && !isSheetOpen) {
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


    var showFavoriteDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val showScrollButton by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
            if (lastItem == null || lastItem.index < layoutInfo.totalItemsCount - 1) {
                true
            } else {
                lastItem.offset + lastItem.size > layoutInfo.viewportEndOffset
            }
        }
    }

    // required variables for the custom scroll system
    val density = LocalDensity.current
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val halfScreenPx = with(density) { screenHeightDp.toPx() * 0.5f }

    var mapHeightPx by remember { mutableFloatStateOf(with(density) { screenHeightDp.toPx() }) }
    val mapHeightDp by remember { derivedStateOf { with(density) { mapHeightPx.toDp() } } }

    // this is needed for the integrated "bottom sheet" to work so that it shrinks the map when scrolled up
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {

            // consume scroll to shrink the map (if it is not already at 0px)
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!isSheetOpen) return Offset.Zero
                if (available.y < 0 && mapHeightPx > 0f && mapHeightPx <= halfScreenPx) {
                    val newHeight = (mapHeightPx + available.y).coerceAtLeast(0f)
                    val consumed = newHeight - mapHeightPx
                    mapHeightPx = newHeight
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            // use remaining scroll
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (!isSheetOpen) return Offset.Zero
                if (available.y > 0) {
                    if (mapHeightPx < halfScreenPx) {
                        val newHeight = (mapHeightPx + available.y).coerceAtMost(halfScreenPx)
                        val consumedDelta = newHeight - mapHeightPx
                        mapHeightPx = newHeight
                        return Offset(0f, consumedDelta)
                    } else if (mapHeightPx >= halfScreenPx) {
                        val resistance = 0.3f
                        val newHeight = (mapHeightPx + available.y * resistance)
                            .coerceAtMost(with(density) { screenHeightDp.toPx() })
                        val consumedDelta = newHeight - mapHeightPx
                        mapHeightPx = newHeight
                        return Offset(0f, consumedDelta)
                    }
                }
                return Offset.Zero
            }

            // used for closing the sheet (with resistance)
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!isSheetOpen) return Velocity.Zero
                if (mapHeightPx > halfScreenPx) {
                    // snap: close if dragged past threshold, otherwise snap back
                    val threshold = halfScreenPx * 1.3f
                    if (mapHeightPx > threshold || available.y > 500f) {
                        isSheetOpen = false
                        mapHeightPx = with(density) { screenHeightDp.toPx() }
                    } else {
                        mapHeightPx = halfScreenPx
                    }
                }
                return Velocity.Zero
            }
        }
    }

    // simple box and column for bottom sheet
    Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().height(mapHeightDp)) {
                MapboxView(
                    onMapClick = { point ->
                        onMapClick(point.latitude(), point.longitude())
                        if (!isSheetOpen) {
                            mapHeightPx = halfScreenPx
                            isSheetOpen = true
                        }
                        isSheetOpen = true
                    },
                    onOverlayError = onOverlayError,
                    onOverlayLoaded = onOverlayLoaded,
                    modifier = Modifier.fillMaxSize(),
                    coordinates = coordinates,
                    searchState = searchState,
                    onSearch = { onSearch() },
                    onSelectResult = onSelectResult
                )
                when (screenState.overlayState) {
                    is OverlayState.Loading -> OverlayStatusCard(message = stringResource(R.string.overlay_loading))
                    is OverlayState.Error -> OverlayRetryCard(message = stringResource(R.string.overlay_error), onRetry = onOverlayRetry)
                    is OverlayState.Success -> Unit
                }
            }

            if (isSheetOpen) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).navigationBarsPadding()
                ) {
                    BottomSheetContent(
                        area = skiArea,
                        climateState = screenState.climateState,
                        dangerLevel = dangerCheckUiState.dangerLevel,
                        modifier = Modifier.fillMaxSize(),
                        startDate = screenState.startDate,
                        endDate = screenState.endDate,
                        selectedYear = screenState.selectedYear,
                        onStartDateChange = { onStartDateChange(it) },
                        onEndDateChange = { onEndDateChange(it) },
                        onYearChange = onYearChange,
                        onAddFavorite = onAddFavorite,
                        canAddFavorite = canAddFavorite,
                        listState = listState
                    )
                    if (showScrollButton) {
                        Button(
                            onClick = {
                                scope.launch {
                                    // hide map
                                    mapHeightPx = 0f
                                    val lastIndex = listState.layoutInfo.totalItemsCount - 1
                                    val lastItemSize = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.size ?: 0
                                    listState.animateScrollToItem(lastIndex, scrollOffset = lastItemSize)
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomCenter).padding(64.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Scroll down"
                            )
                        }
                    }
                }
            }
        }
        BottomBar(modifier = Modifier.align(Alignment.BottomCenter), navController = navController)
    }
}


@Composable
fun BottomSheetContent(
    area: SkiArea,
    climateState: ClimateState,
    dangerLevel: DangerLevelUiModel?,
    modifier: Modifier = Modifier,
    startDate: MonthDay?,
    endDate: MonthDay?,
    selectedYear: Int?,
    onStartDateChange: (MonthDay?) -> Unit,
    onEndDateChange: (MonthDay?) -> Unit,
    onYearChange: (Int?) -> Unit,
    canAddFavorite: () -> Boolean,
    onAddFavorite: () -> Unit,
    listState: LazyListState
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            state = listState
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
                                    },
                                    enabled = canAddFavorite()

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
        navController = rememberNavController(),
        screenState = MapScreenState(
            overlayState = OverlayState.Error,
            climateState = ClimateState.Idle,
            climateCoordinate = null,
        ),
        onMapClick = { _, _ -> },
        coordinates = "",
        onOverlayRetry = {},
        onOverlayLoaded = {},
        onOverlayError = {},
        onStartDateChange = {},
        onEndDateChange = {},
        onYearChange = {},
        modifier = Modifier,
        dangerCheckUiState = DangerCheckUiState(),
        searchState = SearchUiState(),
        onSearch = {},
        onSelectResult = {},
        onAddFavorite = {},
        canAddFavorite = { false }
    )
}


@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
    BottomSheetContent(
        area = previewSkiArea,
        climateState = ClimateState.Idle,
        dangerLevel = DangerLevel.HIGH.toUiModel(),
        startDate = null,
        endDate = null,
        selectedYear = null,
        onStartDateChange = { },
        onEndDateChange = { },
        onYearChange = { },
        onAddFavorite = { },
        listState = rememberLazyListState(),
        canAddFavorite = { false }
    )
}
