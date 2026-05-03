package in2000.prosjekt.in2000project.ui.map

import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapStateHolder {
    private val _pinnedPoint = MutableStateFlow<Point?>(null)
    val pinnedPoint: StateFlow<Point?> = _pinnedPoint.asStateFlow()

    private val _cameraOptions = MutableStateFlow<CameraOptions?>(null)
    val cameraOptions: StateFlow<CameraOptions?> = _cameraOptions.asStateFlow()

    private val _requestedSearchLocation = MutableStateFlow<Point?>(null)
    val requestedSearchLocation: StateFlow<Point?> = _requestedSearchLocation.asStateFlow()

    private val _overlayState = MutableStateFlow<OverlayState>(OverlayState.Success)
    val overlayState: StateFlow<OverlayState> = _overlayState.asStateFlow()

    fun onPinPlaced(point: Point) {
        _pinnedPoint.value = point
    }

    fun onCameraIdle(cameraOptions: CameraOptions) {
        _cameraOptions.value = cameraOptions
    }

    fun onSearchLocationRequested(point: Point) {
        _requestedSearchLocation.value = point
    }

    fun clearRequestedSearchLocation() {
        _requestedSearchLocation.value = null
    }

    fun onOverlayLoading() {
        _overlayState.value = OverlayState.Loading
    }
    fun onOverlayLoaded()  {
        _overlayState.value = OverlayState.Success
    }
    fun onOverlayError()   {
        _overlayState.value = OverlayState.Error
    }
}