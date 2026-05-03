package in2000.prosjekt.in2000project.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.data.repository.FrostAvalancheRiskRepository
import in2000.prosjekt.in2000project.data.repository.NveRepository
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/* dangerLevel is now a DangerLevelUiModel instead of a raw enum.*/
data class RiskUiState(
    val isDanger: Boolean? = null,
    val dangerLevel: DangerLevelUiModel? = null
)
class RiskViewModel(
    private val nveRepository: NveRepository,
    private val frostAvalancheRiskRepository: FrostAvalancheRiskRepository
): ViewModel() {
    private var _riskState = MutableStateFlow(RiskUiState())
    val riskState = _riskState.asStateFlow()

    fun avalancheChecker(lat: Double, lon: Double){
        viewModelScope.launch {

            val geoJson = nveRepository.getGeoJson(lat, lon, 10)
            val isDangerZone = geoJson.features?.isNotEmpty() == true

            val slabDangerLevel = try {
                frostAvalancheRiskRepository.getSlabAvalancheRiskForLocation(
                    Coordinate(lat, lon)
                ).dangerLevel
            } catch(e: Exception) {
                DangerLevel.UNKNOWN
            }

            val dangerLevel = if (isDangerZone) slabDangerLevel.toUiModel() else DangerLevel.LOW.toUiModel()

            _riskState.update { currentState ->
                currentState.copy(
                    isDanger = isDangerZone,
                    dangerLevel = dangerLevel
                )
            }
        }
    }
}
