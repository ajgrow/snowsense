// MapUIState.kt

package in2000.prosjekt.in2000project.ui.map

import in2000.prosjekt.in2000project.data.model.LocationClimateData

data class MapScreenState(
    val overlayState: OverlayState = OverlayState.Loading,
    val climateState: ClimateState = ClimateState.Idle
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