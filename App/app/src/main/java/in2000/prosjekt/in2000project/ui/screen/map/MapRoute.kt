package in2000.prosjekt.in2000project.ui.screen.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.domain.model.ClimateArea
import in2000.prosjekt.in2000project.domain.model.toClimateArea
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import in2000.prosjekt.in2000project.ui.search.SearchViewModel
import in2000.prosjekt.in2000project.ui.risk.RiskViewModel
import org.koin.compose.koinInject

private val previewClimateArea = ClimateArea(
    stationName = "",
    temperatureData = emptyList(),
    precipitationData = emptyList(),
    windSpeedData = emptyList(),
    snowDepthData = emptyList()
)

@Composable
fun MapRoute(
    searchViewModel: SearchViewModel,
    mapViewModel: MapViewModel = koinViewModel(),
    riskViewModel: RiskViewModel = koinViewModel(),
    mapStateHolder: MapStateHolder = koinInject()
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
