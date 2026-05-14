package in2000.prosjekt.in2000project.ui.screen.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.exceptions.NoObservationsException
import in2000.prosjekt.in2000project.domain.repository.ClimateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.MonthDay
import java.time.Year

/**
 * ViewModel for the map screen.
 *
 * @param climateRepository The repository for fetching climate data.
 */
class MapViewModel(
    private val climateRepository: ClimateRepository
) : ViewModel() {

    //Using MapScreenState() instead of <MapScreenState>(MapScreenState.Loading) because
    //it's a data class rather than just one sealed interface. This is because two things can fail instead
    //of just one. Both the WMS overlay can fail and the climate data. This means that
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

    /**
     * Fetches climate data for a given location.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @throws NoObservationsException If there are no observations for the given location.
     */
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

    /**
     * Called when the map is clicked.
     *
     * @param lat The latitude of the clicked location.
     * @param lon The longitude of the clicked location.
     */
    fun onMapClick(lat: Double, lon: Double) {
        _screenState.value = _screenState.value.copy(
            climateCoordinate = Coordinate(lat, lon),
            climateState = ClimateState.Loading,
            isSheetVisible = true
        )
        fetchClimateData(lat, lon)
    }


    /**
     * Called when the start date is changed
     *
     * @param start The new start date
     */
    fun onStartDateChange(start: MonthDay) {
         _screenState.value = _screenState.value.copy(startDate = start)

        refetchIfLocationKnown()
    }

    /**
     * Called when the end date is changed
     *
     * @param end The new end date
     */
    fun onEndDateChange(end: MonthDay) {
        _screenState.value = _screenState.value.copy(endDate = end)

        refetchIfLocationKnown()
    }

    /**
     * Called when the year is changed
     *
     * @param year The new year
     */
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

    /**
     * Called when the sheet is dismissed.
     */
    fun onSheetDismissed() {
        _screenState.value = _screenState.value.copy(
            isSheetVisible = false
        )
    }
}
