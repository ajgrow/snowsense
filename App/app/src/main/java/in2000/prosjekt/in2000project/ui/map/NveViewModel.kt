package in2000.prosjekt.in2000project.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.repository.NveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DangerCheckUiState(
    val isDanger: Boolean? = null
)


class NveViewModel : ViewModel() {

    private val nveRepository = NveRepository()

    private var _dangerCheckUiState = MutableStateFlow(DangerCheckUiState())
    val dangerCheckUiState = _dangerCheckUiState.asStateFlow()

    fun avalancheChecker(lat: Double, lon: Double){
        viewModelScope.launch {

            val geoJson = nveRepository.getGeoJson(lat, lon, 10)
            val isDanger = geoJson.features?.isNotEmpty() == true

            _dangerCheckUiState.update { currentState ->
                currentState.copy(isDanger = isDanger)
            }
        }
    }
}