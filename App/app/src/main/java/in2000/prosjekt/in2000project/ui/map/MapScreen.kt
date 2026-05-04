package in2000.prosjekt.in2000project.ui.map

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.SkiArea
import in2000.prosjekt.in2000project.data.model.toSkiArea
import in2000.prosjekt.in2000project.ui.reusables.ClimateGraph
import in2000.prosjekt.in2000project.ui.reusables.DangerLevelCard
import in2000.prosjekt.in2000project.ui.reusables.DatePicker
import org.koin.compose.viewmodel.koinViewModel
import java.time.MonthDay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapbox.geojson.Point
import in2000.prosjekt.in2000project.data.model.NamedLocation
import in2000.prosjekt.in2000project.ui.search.SearchViewModel
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
import in2000.prosjekt.in2000project.ui.reusables.SearchSection
import in2000.prosjekt.in2000project.ui.reusables.TextInputDialog
import in2000.prosjekt.in2000project.ui.search.SearchUiState
import org.koin.compose.koinInject
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
    riskViewModel: RiskViewModel = koinViewModel(),
) {
    val screenState by mapViewModel.screenState.collectAsState()
    val riskState by riskViewModel.riskState.collectAsState()
    val searchState by searchViewModel.searchState.collectAsStateWithLifecycle()

    val mapStateHolder: MapStateHolder = koinInject()

    val overlayState by mapStateHolder.overlayState.collectAsStateWithLifecycle()

    val pin by mapStateHolder.pinnedPoint.collectAsStateWithLifecycle()
    LaunchedEffect(pin) {
        pin?.let {
            mapViewModel.onMapClick(it.latitude(), it.longitude())
            riskViewModel.avalancheChecker(it.latitude(), it.longitude())
        }
    }

    var showFavoriteDialog by remember { mutableStateOf(false) }

    val skiArea = remember(screenState.climateState) {
        val state = screenState.climateState
        if (state is ClimateState.Success) {
            state.data.toSkiArea()
        } else {
            null
        }
    } ?: previewSkiArea

    if (showFavoriteDialog) {
        TextInputDialog(
            title = stringResource(R.string.name_favorite_prompt),
            defaultValue = when(screenState.climateState) {
                is ClimateState.Success -> {
                    skiArea.name
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

    LaunchedEffect (Unit) {
        mapViewModel.onSheetDismissed()
        MapCameraRepository.clearPinnedPoint()
    }

    MapScreen(
        screenState = screenState,
        riskState = riskState,
        overlayState = overlayState,
        searchState = searchState,
        coordinates = coordinates,
        pinnedPoint = pin,
        onMapClick = { lat, lon ->
            mapViewModel.onMapClick(lat, lon)
            riskViewModel.avalancheChecker(lat, lon)
        },
        onSearchLocationRequested = mapStateHolder::onSearchLocationRequested,
        onZoomIn = mapStateHolder::onZoomIn,
        onZoomOut = mapStateHolder::onZoomOut,
        onOverlayRetry = mapStateHolder::onOverlayLoading,
        onOverlayLoaded = mapStateHolder::onOverlayLoaded,
        onOverlayError = mapStateHolder::onOverlayError,
        onSheetDismiss = mapViewModel::onSheetDismissed,
        onStartDateChange = { it?.let(mapViewModel::onStartDateChange) },
        onEndDateChange = { it?.let(mapViewModel::onEndDateChange) },
        onYearChange = { it?.let(mapViewModel::onYearChange) },
        onSearch = searchViewModel::onSearch ,
        onSelectResult = searchViewModel::addRecentSearch,
        onAddFavorite = {
            screenState.climateCoordinate?.let { coordinate ->
                searchViewModel.addPendingFavorite(coordinate)
                showFavoriteDialog = true } },
        onBottomPaddingChanged = mapStateHolder::onBottomPaddingChanged
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    screenState: MapScreenState,
    riskState: RiskUiState,
    overlayState: OverlayState,
    searchState: SearchUiState,
    coordinates: String,
    pinnedPoint: Point?,
    onMapClick: (Double, Double) -> Unit,
    onSearchLocationRequested: (Point) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onOverlayRetry: () -> Unit,
    onOverlayLoaded: () -> Unit,
    onOverlayError: () -> Unit,
    onSheetDismiss: () -> Unit,
    onStartDateChange: (MonthDay?) -> Unit,
    onEndDateChange: (MonthDay?) -> Unit,
    onYearChange: (Int?) -> Unit,
    onSearch: (String) -> Unit,
    onSelectResult: (NamedLocation) -> Unit,
    onAddFavorite: () -> Unit,
    onBottomPaddingChanged: (Double) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var isSearchActive by remember { mutableStateOf(false) }
    LaunchedEffect(pinnedPoint) {
        if (isSearchActive) {
            isSearchActive = false
            focusManager.clearFocus()
        }
    }
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
    }

    val skiArea = remember(screenState.climateState) {
        val state = screenState.climateState
        if (state is ClimateState.Success) {
            state.data.toSkiArea()
        } else {
            null
        }
    } ?: previewSkiArea

    var startDate by rememberSaveable { mutableStateOf<MonthDay?>(null) }
    var endDate by rememberSaveable { mutableStateOf<MonthDay?>(null) }
    var selectedYear by rememberSaveable { mutableStateOf<Int?>(null) }

    val configuration = LocalWindowInfo.current
    val halfScreen = configuration.containerDpSize.height / 2

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    LaunchedEffect(screenState.climateState) {
        if (screenState.climateState is ClimateState.Success) {
            sheetState.partialExpand()
        }
    }

    val peekHeight = if (screenState.climateState is ClimateState.Success)
                        halfScreen
                    else 0.dp

    val density = LocalDensity.current.density
    LaunchedEffect(peekHeight) {
        onBottomPaddingChanged(peekHeight.value.toDouble() * density)
    }


    Box(
        modifier = modifier.fillMaxSize()
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = { isSearchActive = !isSearchActive },
                modifier = mapButtonModifier,
                contentPadding = PaddingValues(0.dp),
//                enabled = !isSearchActive
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Open Search")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                modifier = mapButtonModifier,
                onClick = onZoomIn
            ) {
                Text(
                    text = "+",
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                modifier = mapButtonModifier,
                onClick = onZoomOut
            ) {
                Text(
                    text = "-",
                    textAlign = TextAlign.Center
                )
            }

        }

        if (isSearchActive) {
            SearchSection(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .padding(top = 24.dp),
                textFieldState = searchState.searchTextFieldState,
                onSearch = onSearch,
                searchResults = searchState.searchResults,
                onNavigateToMap = { coordinate ->
                    onSearchLocationRequested(
                        Point.fromLngLat(
                            coordinate.longitude,
                            coordinate.latitude) )
                      isSearchActive = false
                      focusManager.clearFocus() },
                onSelectResult = onSelectResult,
                recentSearches = searchState.recentSearches
            )
        }

        BottomSheetScaffold(
            containerColor = Color.Transparent,
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekHeight,
            sheetContent = {
                BottomSheetContent(
                    area = skiArea,
                    climateState = screenState.climateState,
                    dangerLevel = riskState.dangerLevel,
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
        overlayState = OverlayState.Error,
        onMapClick = { _, _ -> },
        onSearchLocationRequested = {},
        onZoomIn = {},
        onZoomOut = {},
        coordinates = "",
        pinnedPoint = null,
        onOverlayRetry = {},
        onOverlayLoaded = {},
        onOverlayError = {},
        onSheetDismiss = {},
        onStartDateChange = {},
        onEndDateChange = {},
        onYearChange = {},
        modifier = Modifier,
        riskState = RiskUiState(),
        searchState = SearchUiState(),
        onSearch = {},
        onSelectResult = {},
        onAddFavorite = {},
        onBottomPaddingChanged = {}
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
        onAddFavorite = { }
    )
}
