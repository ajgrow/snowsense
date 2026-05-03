package in2000.prosjekt.in2000project.ui.map

import in2000.prosjekt.in2000project.data.model.LocationClimateData
import java.time.MonthDay
import in2000.prosjekt.in2000project.data.model.Coordinate


data class MapScreenState(
    val overlayState: OverlayState = OverlayState.Loading,
    val climateState: ClimateState = ClimateState.Idle,
    val climateCoordinate: Coordinate? = null,
    val startDate: MonthDay = MonthDay.of(1, 1),
    val endDate: MonthDay = MonthDay.of(12, 31),
    val currentYear: Int = 1,
    val selectedYear: Int = 1,
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