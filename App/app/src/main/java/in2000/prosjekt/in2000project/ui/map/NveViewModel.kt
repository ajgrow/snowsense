package in2000.prosjekt.in2000project.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.data.repository.NveRepository
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/* dangerLevel is now a DangerLevelUiModel instead of a raw enum.*/
data class DangerCheckUiState(
    val isDanger: Boolean? = null,
    val dangerLevel: DangerLevelUiModel? = null
)
class NveViewModel : ViewModel() {

    private val nveRepository = NveRepository()

    private var _dangerCheckUiState = MutableStateFlow(DangerCheckUiState())
    val dangerCheckUiState = _dangerCheckUiState.asStateFlow()

    fun avalancheChecker(lat: Double, lon: Double){
        viewModelScope.launch {

            val geoJson = nveRepository.getGeoJson(lat, lon, 10)
            val isDanger = geoJson.features?.isNotEmpty() == true

            /*
            * WE DO NOT HAVE ALL LOGIC IN PLACE YET
            * At this point we only have the ability to show levels very high
            * or low
            * */
            val dangerLevel = if (isDanger) DangerLevel.VERY_HIGH.toUiModel() else DangerLevel.LOW.toUiModel()

            _dangerCheckUiState.update { currentState ->
                currentState.copy(
                    isDanger = isDanger,
                    dangerLevel = dangerLevel
                )
            }
        }
    }
}
