package in2000.prosjekt.in2000project.ui.screen.map

import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State holder for the map screen.
 */
class MapStateHolder {
    /**
     * Commands to send to the map.
     */
    sealed interface MapCommand {
        /**
         * Fly to a point.
         */
        data class FlyTo(val point: Point, val zoom: Double) : MapCommand

        /**
         * Recenter the map on a point.
         */
        data class Recenter(val point: Point) : MapCommand

        /**
         * Zoom the map by a delta.
         */
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


    /**
     * Place a pin on the map.
     *
     * @param point The point to place the pin at.
     */
    fun placePin(point: Point) {
        _pinnedPoint.value = point
        if (_bottomPaddingPx.value > 0.0) {
            _commands.tryEmit(MapCommand.Recenter(point))
        }
    }

    /**
     * Clear the pin on the map.
     */
    fun clearPin() {
        _pinnedPoint.value = null
    }

    /**
     * Fly to a search result.
     *
     * @param point The point to fly to.
     * @param zoom The zoom level to fly to.
     */
    fun flyToSearch(point: Point, zoom: Double = SEARCH_ZOOM) {
        _pinnedPoint.value = point
        _commands.tryEmit(MapCommand.FlyTo(point, zoom))
    }

    /**
     * Set the bottom padding of the map.
     *
     * @param px The padding in pixels.
     */
    fun setBottomPadding(px: Double) {
        if (_bottomPaddingPx.value == px) return
        _bottomPaddingPx.value = px

        val pointToRecenter = _pinnedPoint.value ?: _cameraOptions.value?.center
        pointToRecenter?.let { _commands.tryEmit(MapCommand.Recenter(it)) }
    }

    /**
     * Called when the camera is idle.
     *
     * @param cameraOptions The camera options.
     */
    fun onCameraIdle(cameraOptions: CameraOptions) {
        _cameraOptions.value = cameraOptions
    }

    fun onOverlayLoading() { _overlayState.value = OverlayState.Loading }

    /**
     * Called when the overlay is successful.
     */
    fun onOverlayError()   { _overlayState.value = OverlayState.Error }

    /**
     * Called when zooming in on the map.
     */
    fun onZoomIn()  { _commands.tryEmit(MapCommand.ZoomBy(+1.0)) }

    /**
     * Called when zooming out on the map.
     */
    fun onZoomOut() { _commands.tryEmit(MapCommand.ZoomBy(-1.0)) }

    companion object {
        const val SEARCH_ZOOM = 12.0
    }
}
