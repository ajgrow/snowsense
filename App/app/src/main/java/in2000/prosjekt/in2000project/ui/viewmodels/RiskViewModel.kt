package in2000.prosjekt.in2000project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.NamedLocation
import in2000.prosjekt.in2000project.domain.usecase.GetAvalancheDangerUseCase
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationRiskState(
    val coordinate: Coordinate,
    val dangerLevel: DangerLevelUiModel? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

data class RiskUiState(
    val dangerLevel: DangerLevelUiModel? = null,
    val locationRisks: Map<Coordinate, LocationRiskState> = emptyMap()
)

class RiskViewModel(
    private val getAvalancheDangerUseCase: GetAvalancheDangerUseCase,
): ViewModel() {
    private var _riskState = MutableStateFlow(RiskUiState())
    val riskState = _riskState.asStateFlow()
    private val loadedFavoriteCoordinates = mutableSetOf<Coordinate>()

    fun refreshFavoriteRisks(favorites: List<NamedLocation>) {
        val toLoad = favorites
            .map { it.coordinate }
            .filterNot { loadedFavoriteCoordinates.contains(it) }

        toLoad.forEach { coordinate ->
            updateDangerLevel(
                coordinate.latitude,
                coordinate.longitude,
                isFavorite = true)

            loadedFavoriteCoordinates += coordinate
        }
    }

    fun updateDangerLevel(lat: Double, lon: Double, isFavorite: Boolean = false){
        val coordinate = Coordinate(lat, lon)

        if (isFavorite) {
            _riskState.update { currentState ->
                val updatedMap = currentState.locationRisks.toMutableMap()
                updatedMap[coordinate] = LocationRiskState(
                    coordinate = coordinate,
                    isLoading = true)
                currentState.copy(locationRisks = updatedMap)
            }
        }

        viewModelScope.launch {
            val dangerLevel = getAvalancheDangerUseCase(coordinate)
                .toUiModel()

            _riskState.update { currentState ->
                if (!isFavorite) {
                    currentState.copy(
                        dangerLevel = dangerLevel
                    )
                } else {
                    val updatedMap = currentState.locationRisks.toMutableMap()
                    updatedMap[coordinate] = LocationRiskState(
                        coordinate = coordinate,
                        dangerLevel = dangerLevel,
                        isLoading = false
                    )
                    currentState.copy(locationRisks = updatedMap)
                }
            }
        }
    }
}