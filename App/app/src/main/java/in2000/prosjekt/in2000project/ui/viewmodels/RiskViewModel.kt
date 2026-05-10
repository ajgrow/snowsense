package in2000.prosjekt.in2000project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.repository.WeatherRepository
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.enums.DangerLevel
import in2000.prosjekt.in2000project.data.repository.NveRepository
import in2000.prosjekt.in2000project.domain.SlabAvalancheRiskAssessor
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
    val isDanger: Boolean? = null,
    val dangerLevel: DangerLevelUiModel? = null,
    val locationRisks: Map<Coordinate, LocationRiskState> = emptyMap()
)
class RiskViewModel(
    private val nveRepository: NveRepository,
    private val weatherRepository: WeatherRepository,
    private val slabAvalancheRiskAssessor: SlabAvalancheRiskAssessor
): ViewModel() {
    private var _riskState = MutableStateFlow(RiskUiState())
    val riskState = _riskState.asStateFlow()

    fun updateDangerLevel(lat: Double, lon: Double, isFavorite: Boolean = false){
        val coordinate = Coordinate(lat, lon)

        if (isFavorite) {
            _riskState.update { currentState ->
                val updatedMap = currentState.locationRisks.toMutableMap()
                updatedMap[coordinate] = LocationRiskState(coordinate = coordinate, isLoading = true)
                currentState.copy(locationRisks = updatedMap)
            }
        }
        viewModelScope.launch {
            val isDangerZone = nveRepository.getGeoJsonStatus(lat, lon, 10)

            val records = try {
                weatherRepository.getWeatherRecordsForLocation(
                    Coordinate(lat, lon),
                    lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
                )
            } catch(e: Exception) {
                emptyList()
            }

            val slabDangerLevel =
                if (records.isNotEmpty())
                    slabAvalancheRiskAssessor.assess(records)
                else
                    (DangerLevel.UNKNOWN)

            val dangerLevel = if (isDangerZone) slabDangerLevel.toUiModel() else DangerLevel.LOW.toUiModel()

            _riskState.update { currentState ->
                if (!isFavorite) {
                    currentState.copy(
                        isDanger = isDangerZone,
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