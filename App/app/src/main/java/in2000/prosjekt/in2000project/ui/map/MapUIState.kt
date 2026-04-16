package in2000.prosjekt.in2000project.ui.map

import in2000.prosjekt.in2000project.data.model.LocationClimateData

sealed interface MapUIState {
    data object Loading : MapUIState
    data class Success(val climateData: LocationClimateData) : MapUIState
    data object Error : MapUIState
}