package in2000.prosjekt.in2000project.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MapUIState>(MapUIState.Loading)
    val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()

    private val _sheetState = MutableStateFlow(MapUISheetState())
    val sheetState = _sheetState.asStateFlow()

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

    fun openSheet(){
        _sheetState.update {
            it.copy(isSheetOpen = true)
        }
    }

    fun closeSheet(){
        _sheetState.update {
            it.copy(
                isSheetOpen = false,
                sheetView = SheetView.RISK
            )
        }
    }

    fun changeSheetView(){
        _sheetState.update {
            it.copy(
                sheetView = if (it.sheetView == SheetView.RISK)
                    SheetView.TRENDS
                else
                    SheetView.RISK
            )
        }
    }
}
