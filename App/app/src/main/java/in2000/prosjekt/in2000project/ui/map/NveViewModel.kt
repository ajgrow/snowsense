package in2000.prosjekt.in2000project.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.model.GeoJson
import in2000.prosjekt.in2000project.data.repository.NveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapScreenUiState(
    val geoJson: GeoJson = GeoJson(
        exceededTransferLimit = false,
        features = emptyList(),
        properties = null,
        type = "FeatureCollection"))

class MapViewModel (): ViewModel() {

    private val nveRepository = NveRepository()
    private val _geoJsonUiState = MutableStateFlow(MapScreenUiState())
    val geoJsonUiState = _geoJsonUiState.asStateFlow()

    fun loadGeoJson(){
        viewModelScope.launch {
            val geoJson = nveRepository.getGeoJson()
            Log.d("MapViewModel", "Antall Features hentet fra NVE: ${geoJson.features?.size?:0}")
            _geoJsonUiState.update { currentState ->
                currentState.copy(geoJson = geoJson)
            }

        }
    }

    init {
        loadGeoJson()
    }
}