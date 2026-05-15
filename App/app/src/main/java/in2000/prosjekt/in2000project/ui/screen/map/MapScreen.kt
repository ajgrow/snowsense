package in2000.prosjekt.in2000project.ui.screen.map

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.domain.model.ClimateArea
import in2000.prosjekt.in2000project.domain.model.NamedLocation
import in2000.prosjekt.in2000project.domain.model.enums.DangerLevel
import in2000.prosjekt.in2000project.domain.model.toClimateArea
import in2000.prosjekt.in2000project.ui.graph.ClimateGraph
import in2000.prosjekt.in2000project.ui.graph.DatePicker
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
import in2000.prosjekt.in2000project.ui.risk.DangerLevelCard
import in2000.prosjekt.in2000project.ui.risk.RiskUiState
import in2000.prosjekt.in2000project.ui.search.SearchDismissScrim
import in2000.prosjekt.in2000project.ui.search.SearchSection
import in2000.prosjekt.in2000project.ui.search.SearchUiState
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.MonthDay
import java.time.Year
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import in2000.prosjekt.in2000project.ui.risk.RiskViewModel
import in2000.prosjekt.in2000project.ui.search.SearchViewModel
import org.koin.compose.viewmodel.koinViewModel

private val previewClimateArea = ClimateArea(
    stationName = "",
    temperatureData = emptyList(),
    precipitationData = emptyList(),
    windSpeedData = emptyList(),
    snowDepthData = emptyList()
)

/**
 * Route-level Composable for the Map screen.
 *
 * @param searchViewModel ViewModel for searching for locations
 * @param riskViewModel ViewModel for getting risk information
 * @param mapStateHolder State holder for the map
 * @param mapViewModel ViewModel for the map
 */
@Composable
fun MapRoute(
    searchViewModel: SearchViewModel,
    riskViewModel: RiskViewModel,
    mapStateHolder: MapStateHolder,
    mapViewModel: MapViewModel = koinViewModel()
) {
    val screenState by mapViewModel.screenState.collectAsStateWithLifecycle()
    val riskState by riskViewModel.riskState.collectAsStateWithLifecycle()
    val searchState by searchViewModel.searchState.collectAsStateWithLifecycle()
    val overlayState by mapStateHolder.overlayState.collectAsStateWithLifecycle()
    val pin by mapStateHolder.pinnedPoint.collectAsStateWithLifecycle()

    LaunchedEffect(pin) {
        pin?.let {
            mapViewModel.onMapClick(it.latitude(), it.longitude())
            riskViewModel.updateDangerLevel(it.latitude(), it.longitude())
        }
    }

    var showFavoriteDialog by remember { mutableStateOf(false) }

    val climateArea = remember(screenState.climateState) {
        val state = screenState.climateState
        if (state is ClimateState.Success) {
            state.data.toClimateArea()
        } else {
            null
        }
    } ?: previewClimateArea

    if (showFavoriteDialog) {
        TextInputDialog(
            title = stringResource(R.string.name_favorite_prompt),
            defaultValue = when (screenState.climateState) {
                is ClimateState.Success -> {
                    climateArea.stationName ?: stringResource(R.string.missing_station_name)
                }

                else -> {
                    ""
                }
            },
            onConfirm = { name ->
                if (name.isNotBlank()) {
                    searchViewModel.nameFavorite(name)
                    showFavoriteDialog = false
                }
            },
            onDismiss = { showFavoriteDialog = false }
        )
    }

    LaunchedEffect(Unit) {
        searchViewModel.clearQuery()
    }

    MapScreen(
        screenState = screenState,
        riskState = riskState,
        overlayState = overlayState,
        searchState = searchState,
        onSearchLocationRequested = mapStateHolder::flyToSearch,
        onZoomIn = mapStateHolder::onZoomIn,
        onZoomOut = mapStateHolder::onZoomOut,
        onOverlayRetry = mapStateHolder::onOverlayLoading,
        onSheetDismiss = {
            mapViewModel.onSheetDismissed()
            mapStateHolder.clearPin()
        },
        onStartDateChange = { it?.let(mapViewModel::onStartDateChange) },
        onEndDateChange = { it?.let(mapViewModel::onEndDateChange) },
        onYearChange = { it?.let(mapViewModel::onYearChange) },
        onQueryChange = searchViewModel::onQueryChange,
        onSelectResult = searchViewModel::selectSearchResult,
        onAddFavorite = {
            screenState.climateCoordinate?.let { coordinate ->
                searchViewModel.addPendingFavorite(coordinate)
                showFavoriteDialog = true
            }
        },
        onBottomPaddingChanged = mapStateHolder::setBottomPadding,
        canAddFavorite = {
            val coordinate = screenState.climateCoordinate
            if (coordinate != null) {
                !searchState.favorites.any { it.coordinate == coordinate }
            } else {
                false
            }
        },
        onSearchActiveChange = searchViewModel::setSearchActive
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapScreen(
    modifier: Modifier = Modifier,
    screenState: MapScreenState,
    riskState: RiskUiState,
    overlayState: OverlayState,
    searchState: SearchUiState,
    onSearchLocationRequested: (Point) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onOverlayRetry: () -> Unit,
    onSheetDismiss: () -> Unit,
    onStartDateChange: (MonthDay?) -> Unit,
    onEndDateChange: (MonthDay?) -> Unit,
    onYearChange: (Int?) -> Unit,
    onQueryChange: (query: String) -> Unit,
    onSelectResult: (NamedLocation) -> Unit,
    onAddFavorite: () -> Unit,
    canAddFavorite: () -> Boolean,
    onBottomPaddingChanged: (Double) -> Unit,
    onSearchActiveChange: (isActive: Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val isSearchActive = searchState.isSearchActive

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
    }

    val skiArea = remember(screenState.climateState) {
        val state = screenState.climateState
        if (state is ClimateState.Success) {
            state.data.toClimateArea()
        } else {
            null
        }
    } ?: previewClimateArea

    val density = LocalDensity.current

    val halfScreen = LocalWindowInfo.current.containerDpSize.height / 2

    val navigationBarHeight = WindowInsets.navigationBars.getBottom(density)

    //the distance from the top of the sheet to the top of the screen
    var sheetOffsetPx by remember { mutableFloatStateOf(0f) }
    //what sheetOffsetPx would be when the sheet is hidden
    var sheetHiddenOffsetPx by remember { mutableStateOf<Float?>(null) }
    //how many pixels above hidden position the sheet is
    //used for accurate padding accounting for the navigation bar
    val sheetCoveredPx =
        if (sheetHiddenOffsetPx == null)
            0f
        else
            (sheetHiddenOffsetPx!! - sheetOffsetPx).coerceAtLeast(0f)

    val bottomPaddingDp = with(density) {
        sheetCoveredPx.toDp()
    }

    val showSheet = screenState.isSheetVisible
    val peekHeight = if (showSheet) halfScreen else 0.dp

    val sheetState = rememberStandardBottomSheetState(
        initialValue = if (showSheet) SheetValue.Expanded else SheetValue.Hidden,
        skipHiddenState = false,
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    val listState = rememberLazyListState()

    val scope = rememberCoroutineScope()

    val atTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0
        }
    }

    // used to prevent dragging the sheet when it should be scrolled
    val nestedScrollConnection = remember(atTop) {
        object : NestedScrollConnection {
        }
    }

    val showScrollButton by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()

            if (layoutInfo.totalItemsCount == 0 || lastItem == null) return@derivedStateOf false

            if (lastItem.index < layoutInfo.totalItemsCount - 1) {
                true
            } else {
                lastItem.offset + lastItem.size > layoutInfo.viewportEndOffset
            }
        }
    }

    val onScrollButtonClicked: () -> Unit = {
        scope.launch {
            launch { sheetState.expand() }
            launch {
                snapshotFlow { listState.layoutInfo.viewportEndOffset }
                    .first()
                listState.animateScrollBy(listState.layoutInfo.totalItemsCount * 10_000f)
            }
        }
    }

    LaunchedEffect(peekHeight) {
        onBottomPaddingChanged((peekHeight.value.toDouble() + navigationBarHeight) * density.density)
    }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }
            .drop(1)
            .collect { value ->
                if (value == SheetValue.Hidden) {
                    onSheetDismiss()
                    listState.scrollToItem(0)
                }
            }
    }

    LaunchedEffect(sheetState) {
        snapshotFlow {
            runCatching { sheetState.requireOffset() }.getOrNull()
        }.collect { offset ->
            if (offset != null) {
                sheetOffsetPx = offset
                if (sheetHiddenOffsetPx == null) {
                    sheetHiddenOffsetPx = offset
                }
            }
        }
    }

    LaunchedEffect(showSheet) {
        if (showSheet) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }



    val mapDesc = stringResource(R.string.map_title)

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                paneTitle = mapDesc
            }
    ) {
        when (overlayState) {
            is OverlayState.Loading
                -> OverlayStatusCard(
                stringResource(R.string.overlay_loading))
            is OverlayState.Error
                -> OverlayRetryCard(
                stringResource(R.string.overlay_error),
                onOverlayRetry)
            is OverlayState.Success
                -> Unit
        }

        val mapButtonModifier = Modifier.size(50.dp)

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = bottomPaddingDp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = { onSearchActiveChange(!isSearchActive) },
                modifier = mapButtonModifier,
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.open_search)
                )
            }

            Spacer(Modifier.height(8.dp))

            val zoomInDesc = stringResource(R.string.zoom_in_btn_desc)

            Button(
                modifier = mapButtonModifier
                    .semantics {
                        contentDescription = zoomInDesc
                    },
                onClick = onZoomIn,
            ) {
                Text(
                    modifier = Modifier.clearAndSetSemantics { },
                    text = "+",
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(8.dp))

            val zoomOutDesc = stringResource(R.string.zoom_out_btn_desc)

            Button(
                modifier = mapButtonModifier
                    .semantics {
                        contentDescription = zoomOutDesc
                    },
                onClick = onZoomOut
            ) {
                Text(
                    modifier = Modifier.clearAndSetSemantics { },
                    text = "-",
                    textAlign = TextAlign.Center
                )
            }

        }

        BottomSheetScaffold(
            containerColor = Color.Transparent,
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekHeight,
            sheetSwipeEnabled = atTop,
            sheetContent = {
                BottomSheetContent(
                    area = skiArea,
                    climateState = screenState.climateState,
                    dangerLevel = riskState.dangerLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 320.dp),
                    startDate = screenState.startDate,
                    endDate = screenState.endDate,
                    selectedYear = screenState.selectedYear,
                    onStartDateChange = { onStartDateChange(it) },
                    onEndDateChange = { onEndDateChange(it) },
                    onYearChange = onYearChange,
                    onAddFavorite = onAddFavorite,
                    canAddFavorite = canAddFavorite,
                    listState = listState,
                    nestedScrollConnection = nestedScrollConnection
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
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

        val isSheetTargetingHidden = sheetState.targetValue == SheetValue.Hidden

        if (showScrollButton && showSheet && !isSheetTargetingHidden) {
            Button(
                onClick = onScrollButtonClicked,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .navigationBarsPadding()
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.scroll_down)
                )
            }
        }

        // Rendered last so it sits above the map, side buttons, BottomSheetScaffold, and scroll
        // button — any tap outside the search bar's bounds reaches the scrim and dismisses.
        SearchDismissScrim(
            isActive = isSearchActive,
            onDismiss = { onSearchActiveChange(false) }
        )

        if (isSearchActive) {
            SearchSection(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .padding(top = 120.dp),
                searchState = searchState,
                onNavigateToMap = { coordinate ->
                    onSearchLocationRequested(
                        Point.fromLngLat(
                            coordinate.longitude,
                            coordinate.latitude
                        )
                    )
                    onSearchActiveChange(false)
                    focusManager.clearFocus()
                },
                onSelectResult = onSelectResult,
                onActiveChange = onSearchActiveChange,
                onQueryChange = onQueryChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetContent(
    area: ClimateArea,
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
    listState: LazyListState,
    nestedScrollConnection: NestedScrollConnection
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .nestedScroll(nestedScrollConnection),
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
                            Text(
                                text = stringResource(R.string.loading),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        is ClimateState.Error -> {
                            Text(
                                text = stringResource(R.string.climate_error_desc),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        else -> {
                            Column(
                                modifier = Modifier.semantics(mergeDescendants = true) { }
                            ) {
                                Text(
                                    text = stringResource(R.string.weather_station_desc),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = area.stationName
                                        ?: stringResource(R.string.missing_station_name),
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DangerLevelCard(uiModel = dangerLevel)
                                IconButton(
                                    enabled = canAddFavorite(),
                                    modifier = Modifier,
                                    onClick = {
                                        onAddFavorite()
                                    }

                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = stringResource(R.string.favorite_button_desc)
                                    )
                                }
                            }
                        }
                    }
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
                        Text(text = stringResource(R.string.no_date_selected_msg))
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

                        HorizontalDivider()

                        ClimateGraph(
                            dataPoints = area.snowDepthData,
                            startDate = startDate,
                            endDate = endDate,
                            title = stringResource(R.string.mean_snow_desc)
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
            selectedYear = 2023,
            currentYear = 2026
        ),
        overlayState = OverlayState.Error,
        onSearchLocationRequested = {},
        onZoomIn = {},
        onZoomOut = {},
        onOverlayRetry = {},
        onSheetDismiss = {},
        onStartDateChange = {},
        onEndDateChange = {},
        onYearChange = {},
        modifier = Modifier,
        riskState = RiskUiState(),
        searchState = SearchUiState(),
        onQueryChange = {},
        onSelectResult = {},
        onAddFavorite = {},
        canAddFavorite = { false },
        onBottomPaddingChanged = {},
        onSearchActiveChange = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
    BottomSheetContent(
        area = previewClimateArea,
        climateState = ClimateState.Idle,
        dangerLevel = DangerLevel.HIGH.toUiModel(),
        startDate = null,
        endDate = null,
        selectedYear = null,
        onStartDateChange = { },
        onEndDateChange = { },
        onYearChange = { },
        onAddFavorite = { },
        canAddFavorite = { false },
        listState = rememberLazyListState(),
        nestedScrollConnection = rememberNestedScrollInteropConnection()
    )
}
