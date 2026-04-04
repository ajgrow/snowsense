package in2000.prosjekt.in2000project.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.model.GeoJson
import in2000.prosjekt.in2000project.data.repository.NveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapScreenUiState(
    val geoJson: GeoJson = GeoJson(emptyList()),
)

class MapViewModel (
    private val nveRepository: NveRepository
): ViewModel() {
    private val _geoJsonUiState = MutableStateFlow(MapScreenUiState())
    val geoJsonUiState = _geoJsonUiState.asStateFlow()

    fun loadGeoJson(){
        viewModelScope.launch {
            val geoJson = nveRepository.getGeoJson()

            _geoJsonUiState.update { currentState ->
                currentState.copy(geoJson = geoJson)
            }

        }
    }
}