package in2000.prosjekt.in2000project.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MapUIState>(MapUIState.Loading)
    val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()

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
