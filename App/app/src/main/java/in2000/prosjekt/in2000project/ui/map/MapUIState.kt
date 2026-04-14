package in2000.prosjekt.in2000project.ui.map

sealed interface MapUIState {
    data object Loading : MapUIState
    data class Success(val geoJson: String) : MapUIState
    data object Error : MapUIState
}