// MapUIState.kt

package in2000.prosjekt.in2000project.ui.map

import in2000.prosjekt.in2000project.data.model.LocationClimateData
import java.time.MonthDay

data class MapScreenState(
    val overlayState: OverlayState = OverlayState.Loading,
    val climateState: ClimateState = ClimateState.Idle,
    val coordinates: String = "65.0,10.0",
    val periodStart: MonthDay = MonthDay.now(),
    val periodEnd: MonthDay = MonthDay.now(),
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