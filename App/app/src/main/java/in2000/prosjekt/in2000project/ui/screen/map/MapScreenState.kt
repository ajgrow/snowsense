package in2000.prosjekt.in2000project.ui.screen.map

import in2000.prosjekt.in2000project.domain.model.LocationClimateData
import java.time.MonthDay
import in2000.prosjekt.in2000project.domain.model.Coordinate


data class MapScreenState(
    val overlayState: OverlayState = OverlayState.Loading,
    val climateState: ClimateState = ClimateState.Idle,
    val climateCoordinate: Coordinate? = null,
    val startDate: MonthDay,
    val endDate: MonthDay,
    val currentYear: Int,
    val selectedYear: Int,
    val isSheetVisible: Boolean = false
)

sealed interface OverlayState {
    object Loading : OverlayState
    object Success : OverlayState
    object Error : OverlayState
}

sealed interface ClimateState {
    object Idle : ClimateState
    object Loading : ClimateState
    data class Success(val data: LocationClimateData) : ClimateState
    object Error : ClimateState
}