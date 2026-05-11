package in2000.prosjekt.in2000project.ui.screen.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.model.exceptions.NoObservationsException
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.repository.ClimateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.MonthDay
import java.time.Year

class MapViewModel(
    private val climateRepository: ClimateRepository
) : ViewModel() {

    //Using MapScreenState() instead of <MapScreenState>(MapScreenState.Loading) because
    //it's a data class rather than just one sealed interface. This is because two things can fail instead
    //of just one. Both the WMS overlay can fail as well as the climate data. This means that
    //we base this off the MapScreenState() data class rather than just one sealed interface.
    private val _screenState = MutableStateFlow(MapScreenState(
        startDate = MonthDay.now().with(MonthDay.now().month.minus(1)),
        endDate = MonthDay.now(),
        currentYear = Year.now().value,
        selectedYear = Year.now().value - 3
    ))
    val screenState: StateFlow<MapScreenState> = _screenState.asStateFlow()

    //.copy() creates a new instance of the data class with a few of the properties changed while
    //everything else stays the same. StateFlow needs to identify the change but can't if you
    //just mutate the existing object in place and the UI won't update. However, if you create a copy(),
    //the old value is replaced with a new one which triggers recomposition

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

    //set climateState to Loading until success.
    //use coordinates from UI to call getClimateDataForLocation() using coordinates and time period
    fun fetchClimateData(lat: Double, lon: Double) {
        viewModelScope.launch {
            _screenState.value = _screenState.value.copy(
                climateState = ClimateState.Loading
            )

            try {
                val current = _screenState.value
                val climateData = climateRepository.getClimateDataForLocation(
                    coordinate = Coordinate(lat, lon),
                    periodStart = current.startDate,
                    periodEnd = current.endDate,
                    yearsBack = current.currentYear - current.selectedYear
                ) ?: throw NoObservationsException("","","")

                _screenState.value = _screenState.value.copy(
                    climateState = ClimateState.Success(climateData)
                )
            } catch (e: Exception) {
                Log.d("ClimateData", "Something went wrong: " + Log.getStackTraceString(e))
                e.printStackTrace()

                _screenState.value = _screenState.value.copy(
                    climateState = ClimateState.Error
                )
            }
        }
    }

    //when user taps map, fetches the info with the default date range
    fun onMapClick(lat: Double, lon: Double) {
        _screenState.value = _screenState.value.copy(
            climateCoordinate = Coordinate(lat, lon),
            climateState = ClimateState.Loading,
            isSheetVisible = true
        )
        fetchClimateData(lat, lon)
    }

    fun onStartDateChange(start: MonthDay) {
        val current = _screenState.value
        val end = setTimeBoundary(start, current.endDate)

        _screenState.value = _screenState.value.copy(
            startDate = start
            //TIME BOUNDARY NOT IMPLEMENTED YET
            //periodEnd = end
        )

        refetchIfLocationKnown()
    }

    fun onEndDateChange(end: MonthDay) {
        val current = _screenState.value
        val start = setTimeBoundary(current.startDate, end)

        _screenState.value = _screenState.value.copy(
            //TIME BOUNDARY NOT IMPLEMENTED YET
            //periodStart = start,
            endDate = end
        )

        refetchIfLocationKnown()
    }

    fun onYearChange(year: Int) {
        val updated = _screenState.value.copy(selectedYear = year)
        _screenState.value = updated

        val coordinate = updated.climateCoordinate ?: return
        fetchClimateData(coordinate.latitude, coordinate.longitude)
    }

    private fun refetchIfLocationKnown() {
        val coordinate = _screenState.value.climateCoordinate ?: return
        fetchClimateData(coordinate.latitude, coordinate.longitude)
    }

    private fun setTimeBoundary(start: MonthDay, end: MonthDay): MonthDay {
        // TO DO - enforce max 1 month range
        return end
    }

    fun onSheetDismissed() {
        _screenState.value = _screenState.value.copy(
            isSheetVisible = false
        )
    }
}
