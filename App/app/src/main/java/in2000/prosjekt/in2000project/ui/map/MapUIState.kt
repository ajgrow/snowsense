package in2000.prosjekt.in2000project.ui.map

sealed interface MapUIState {
    data class Success() : MapUIState
    object RetryAction : MapUIState
    object Error : MapUIState
    object Loading : MapUIState
}