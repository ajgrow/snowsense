package in2000.prosjekt.in2000project.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.repository.FrostClimateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.MonthDay

class MapViewModel(
    private val frostClimateRepository: FrostClimateRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow(MapScreenState())
    val screenState: StateFlow<MapScreenState> = _screenState.asStateFlow()


    fun updateLocation(coordinates: String) {
        _screenState.value = _screenState.value.copy(
            coordinates = coordinates
        )
    }

    fun onOverlayLoading() {
        _screenState.value = _screenState.value.copy(
            overlayState = OverlayState.Loading
        )
    }

    fun onOverlayLoaded() {
        _screenState.value = _screenState.value.copy(
            overlayState = OverlayState.Success
        )
    }

    fun onOverlayError() {
        _screenState.value = _screenState.value.copy(
            overlayState = OverlayState.Error
        )
    }

    fun fetchClimateData(lat: Double, lon: Double) {
        viewModelScope.launch {
            _screenState.value = _screenState.value.copy(
                climateState = ClimateState.Loading
            )

            val today = LocalDate.now()
            val twoWeeksAgo = today.minusDays(14)

            _screenState.value = try {
                val climateData = frostClimateRepository.getClimateDataForLocation(
                    coordinate = Coordinate(lat, lon),
                    periodStart = MonthDay.from(twoWeeksAgo),
                    periodEnd = MonthDay.from(today),
                    yearsBack = 0
                )

                _screenState.value.copy(
                    climateState = ClimateState.Success(climateData)
                )
            } catch (e: Exception) {
                e.printStackTrace()

                _screenState.value.copy(
                    climateState = ClimateState.Error
                )
            }
        }
    }
}