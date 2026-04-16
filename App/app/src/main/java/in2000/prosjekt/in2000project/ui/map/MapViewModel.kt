package in2000.prosjekt.in2000project.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.repository.GeoJsonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MapUIState>(MapUIState.Loading)
    val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()

    var coordinates by mutableStateOf("65.0,10.0")

    fun updateLocation(coordinates: String) {
        this.coordinates = coordinates
    }

    init {
        getMapData()
    }

    fun getMapData() {
        viewModelScope.launch {
            _uiState.value = MapUIState.Loading

            _uiState.value = try {
                MapUIState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                MapUIState.Error
            }
        }
    }

    fun onOverlayLoaded() {
        _uiState.value = MapUIState.Success
    }

    fun onOverlayError() {
        _uiState.value = MapUIState.Error
    }
}
