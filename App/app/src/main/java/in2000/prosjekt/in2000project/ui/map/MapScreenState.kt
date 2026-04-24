package in2000.prosjekt.in2000project.ui.map

import in2000.prosjekt.in2000project.data.model.LocationClimateData
import java.time.MonthDay
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.SkiArea


data class MapScreenState(
    val overlayState: OverlayState = OverlayState.Loading,
    val climateState: ClimateState = ClimateState.Idle,
    val climateCoordinate: Coordinate? = null,
    val periodStart: MonthDay = MonthDay.of(1, 1),
    val periodEnd: MonthDay = MonthDay.of(12, 31),
    val yearsBack: Int = 1
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