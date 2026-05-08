package in2000.prosjekt.in2000project.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.NoObservationsException
import in2000.prosjekt.in2000project.data.repository.FrostClimateRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.MonthDay
import java.time.Year

class MapViewModel(
    private val frostClimateRepository: FrostClimateRepository
) : ViewModel() {

    //Using MapScreenState() instead of <MapScreenState>(MapScreenState.Loading) because
    //it's a data class rather than just one sealed interface. This is because two things can fail instead
    //of just one. Both the WMS overlay can fail as well as the climate data. This means that
    //we base this off the MapScreenState() data class rather than just one sealed interface.
    private val _screenState = MutableStateFlow(MapScreenState())
    val screenState: StateFlow<MapScreenState> = _screenState.asStateFlow()

    sealed interface MapAction {
        data class FlyTo(
            val point: Point,
            val zoom: Double
        ) : MapAction

        data class Recenter(
            val point: Point
        ) : MapAction

        data class ZoomBy(
            val delta: Double
        ) : MapAction
    }

    companion object {
        const val SEARCH_ZOOM = 12.0
    }
    private val _mapActions = MutableSharedFlow<MapAction>(extraBufferCapacity = 8)
    val mapActions: SharedFlow<MapAction> = _mapActions.asSharedFlow()


    init {
        val now = MonthDay.now()
        val currentYear = Year.now().value
        _screenState.value = _screenState.value.copy(
            startDate = now.with(now.month.minus(1)),
            endDate = now,
            currentYear = currentYear,
            selectedYear = currentYear - 3
        )
    }

    //.copy() creates a new instance of the data class with a few of the properties changed while
    //everything else stays the same. StateFlow needs to identify the change but can't if you
    //just mutate the existing object in place and the UI won't update. However, if you create a copy(),
    //the old value is replaced with a new one which triggers recomposition

    //set climateState to Loading until success.
    //use coordinates from UI to call getClimateDataForLocation() using coordinates and time period
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

    fun onCameraIdle(cameraOptions: CameraOptions) {
        _screenState.value = _screenState.value.copy(
            cameraOptions = cameraOptions
        )
    }

    fun setBottomPadding(px: Double) {
        val current = _screenState.value

        if (current.bottomPaddingPx == px) return

        val updated = current.copy(
            bottomPaddingPx = px
        )

        _screenState.value = updated

        val pointToRecenter =
            updated.pinnedPoint ?: updated.cameraOptions?.center

        pointToRecenter?.let {
            _mapActions.tryEmit(MapAction.Recenter(it))
        }
    }

    fun onZoomIn() {
        _mapActions.tryEmit(
            MapAction.ZoomBy(+1.0)
        )
    }

    fun onZoomOut() {
        _mapActions.tryEmit(
            MapAction.ZoomBy(-1.0)
        )
    }
    fun placePin(point: Point) {
        val current = _screenState.value

        _screenState.value = current.copy(
            pinnedPoint = point
        )

        if (current.bottomPaddingPx > 0.0) {
            _mapActions.tryEmit(
                MapAction.Recenter(point)
            )
        }
    }

    fun clearPin() {
        _screenState.value = _screenState.value.copy(
            pinnedPoint = null
        )
    }

    fun flyToSearch(
        point: Point,
        zoom: Double = SEARCH_ZOOM
    ) {
        _screenState.value = _screenState.value.copy(
            pinnedPoint = point
        )

        _screenState.value = _screenState.value.copy(
            pinnedPoint = point,
            climateCoordinate = Coordinate(point.latitude(), point.longitude()),
            isSheetVisible = true,
            climateState = ClimateState.Loading
        )

        _mapActions.tryEmit(
            MapAction.FlyTo(point, zoom)
        )

        fetchClimateData(point.latitude(), point.longitude())
    }

    fun fetchClimateData(lat: Double, lon: Double) {
        viewModelScope.launch {
            _screenState.value = _screenState.value.copy(
                climateState = ClimateState.Loading
            )

            try {
                val current = _screenState.value
                val climateData = frostClimateRepository.getClimateDataForLocation(
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

    fun onMapClick(
        lat: Double,
        lon: Double
    ) {
        val point = Point.fromLngLat(lon, lat)
        placePin(point)

        _screenState.value = _screenState.value.copy(
            climateCoordinate = Coordinate(lat, lon),
            climateState = ClimateState.Loading,
            isSheetVisible = true
        )
        fetchClimateData(lat, lon)
    }

    fun onStartDateChange(start: MonthDay) {
        val current = _screenState.value

        _screenState.value = _screenState.value.copy(
            startDate = start
            //TIME BOUNDARY NOT IMPLEMENTED YET
            //periodEnd = end
        )

        refetchIfLocationKnown()
    }

    fun onEndDateChange(end: MonthDay) {
        val current = _screenState.value

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