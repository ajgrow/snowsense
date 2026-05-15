package in2000.prosjekt.in2000project.ui.risk

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

/**
 * Represents the state of a location's risk.
 *
 * @property coordinate The coordinate of the location.
 * @property dangerLevel The danger level of the location.
 * @property isLoading Whether the danger level is being loaded.
 * @property isError Whether an error occurred while loading the danger level.
 */
data class LocationRiskState(
    val coordinate: Coordinate,
    val dangerLevel: DangerLevelUiModel? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

/**
 * Represents the state of the risk screen.
 *
 * @property dangerLevel The danger level of the current location.
 * @property locationRisks A map of coordinates to their corresponding risk states.
 */
data class RiskUiState(
    val dangerLevel: DangerLevelUiModel? = null,
    val locationRisks: Map<Coordinate, LocationRiskState> = emptyMap()
)

/**
 * Shared ViewModel for risk.
 *
 * @param getAvalancheDangerUseCase The use case for getting the avalanche danger.
 */
class RiskViewModel(
    private val getAvalancheDangerUseCase: GetAvalancheDangerUseCase,
) : ViewModel() {
    private var _riskState = MutableStateFlow(RiskUiState())
    val riskState = _riskState.asStateFlow()
    private val loadedFavoriteCoordinates = mutableSetOf<Coordinate>()

    /**
     * Refreshes the favorite risks.
     *
     * @param favorites The list of favorite locations.
     */
    fun refreshFavoriteRisks(favorites: List<NamedLocation>) {
        val toLoad = favorites
            .map { it.coordinate }
            .filterNot { loadedFavoriteCoordinates.contains(it) }

        toLoad.forEach { coordinate ->
            updateDangerLevel(
                coordinate.latitude,
                coordinate.longitude,
                isFavorite = true
            )

            loadedFavoriteCoordinates += coordinate
        }
    }

    /**
     * Updates the danger level of a location.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @param isFavorite Whether the location is a favorite.
     */
    fun updateDangerLevel(lat: Double, lon: Double, isFavorite: Boolean = false) {
        val coordinate = Coordinate(lat, lon)

        if (isFavorite) {
            _riskState.update { currentState ->
                val updatedMap = currentState.locationRisks.toMutableMap()
                updatedMap[coordinate] = LocationRiskState(
                    coordinate = coordinate,
                    isLoading = true
                )
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