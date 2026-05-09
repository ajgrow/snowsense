package in2000.prosjekt.in2000project.ui.screens.map

import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class MapStateHolder {

    sealed interface MapCommand {
        data class FlyTo(val point: Point, val zoom: Double) : MapCommand
        data class Recenter(val point: Point) : MapCommand
        data class ZoomBy(val delta: Double) : MapCommand
    }

    private val _commands = MutableSharedFlow<MapCommand>(
        extraBufferCapacity = 8
    )
    val commands: SharedFlow<MapCommand> = _commands.asSharedFlow()

    private val _pinnedPoint = MutableStateFlow<Point?>(null)
    val pinnedPoint: StateFlow<Point?> = _pinnedPoint.asStateFlow()

    private val _cameraOptions = MutableStateFlow<CameraOptions?>(null)
    val cameraOptions: StateFlow<CameraOptions?> = _cameraOptions.asStateFlow()

    private val _overlayState = MutableStateFlow<OverlayState>(OverlayState.Success)
    val overlayState: StateFlow<OverlayState> = _overlayState.asStateFlow()

    private val _bottomPaddingPx = MutableStateFlow(0.0)
    val bottomPaddingPx: StateFlow<Double> = _bottomPaddingPx.asStateFlow()

    fun placePin(point: Point) {
        _pinnedPoint.value = point
        if (_bottomPaddingPx.value > 0.0) {
            _commands.tryEmit(MapCommand.Recenter(point))
        }
    }

    fun clearPin() {
        _pinnedPoint.value = null
    }

    fun flyToSearch(point: Point, zoom: Double = SEARCH_ZOOM) {
        _pinnedPoint.value = point
        _commands.tryEmit(MapCommand.FlyTo(point, zoom))
    }

    fun setBottomPadding(px: Double) {
        if (_bottomPaddingPx.value == px) return
        _bottomPaddingPx.value = px

        val pointToRecenter = _pinnedPoint.value ?: _cameraOptions.value?.center
        pointToRecenter?.let { _commands.tryEmit(MapCommand.Recenter(it)) }
    }

    fun onCameraIdle(cameraOptions: CameraOptions) {
        _cameraOptions.value = cameraOptions
    }

    fun onOverlayLoading() { _overlayState.value = OverlayState.Loading }
    fun onOverlayLoaded()  { _overlayState.value = OverlayState.Success }
    fun onOverlayError()   { _overlayState.value = OverlayState.Error }

    fun onZoomIn()  { _commands.tryEmit(MapCommand.ZoomBy(+1.0)) }
    fun onZoomOut() { _commands.tryEmit(MapCommand.ZoomBy(-1.0)) }

    companion object {
        const val SEARCH_ZOOM = 12.0
    }
}
