package in2000.prosjekt.in2000project.ui.map

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mapbox.common.Cancelable
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.rasterSource
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.toCameraOptions

object MapCameraRepository {
    var lastCameraOptions: CameraOptions? = null
}
private const val TAG = "SnoskredWMS"

private const val WMS_BASE =
    "https://kart.nve.no/enterprise/services/SnoskredAktsomhet/MapServer/WMSServer"

// Overview layers
private const val WMS_OVERVIEW =
    "$WMS_BASE?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap" +
            "&BBOX={bbox-epsg-3857}&CRS=EPSG:3857&WIDTH=256&HEIGHT=256" +
            "&LAYERS=PotensieltSkredfareOmr" +
            "&STYLES=&FORMAT=image/png&TRANSPARENT=TRUE"

// Detail layers
private const val WMS_DETAIL =
    "$WMS_BASE?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap" +
            "&BBOX={bbox-epsg-3857}&CRS=EPSG:3857&WIDTH=256&HEIGHT=256" +
            "&LAYERS=" +
            "S2_snoskred_m_skogeffekt_Aktsomhetsomrade," +
            "&STYLES=&FORMAT=image/png&TRANSPARENT=TRUE"

@Composable
fun MapboxMapScreen(
    onMapClick: (Point) -> Unit,
    coordinates: String
) {
    val (hasSearch, searchLat, searchLng) = remember(coordinates) {
        if (coordinates.isNotBlank() && coordinates != "0,0") {
            val parts = coordinates.split(",")
            if (parts.size == 2) {
                Triple(true, parts[0].toDoubleOrNull(), parts[1].toDoubleOrNull())
            } else Triple(false, null, null)
        } else Triple(false, null, null)
    }

    val initialCamera = remember(hasSearch, searchLat, searchLng) {
        if (hasSearch && searchLat != null && searchLng != null) {
            CameraOptions.Builder()
                .center(Point.fromLngLat(searchLng, searchLat))
                .zoom(12.0)
                .build()
                .also { MapCameraRepository.lastCameraOptions = it }
        } else {
            MapCameraRepository.lastCameraOptions ?: CameraOptions.Builder()
                .center(Point.fromLngLat(10.75, 59.91))
                .zoom(3.5)
                .build()
        }
    }

    val viewportState = rememberMapViewportState {
        setCameraOptions(initialCamera)
    }

    var idleSubscription by remember { mutableStateOf<Cancelable?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            viewportState.cameraState?.toCameraOptions()?.let {
                MapCameraRepository.lastCameraOptions = it
            }
            idleSubscription?.cancel()
        }
    }

    val mapRecreateKey = remember(hasSearch, searchLat, searchLng) {
        if (hasSearch) System.currentTimeMillis() else "default"
    }



    DisposableEffect(Unit) {
        onDispose {

        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        key(mapRecreateKey){
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = viewportState
            ) {
                //this click listener just works generally on the map
                MapEffect(Unit) { mapView ->
                    mapView.gestures.addOnMapClickListener { point ->

                        onMapClick(point)
                        true //return true to show that the click was handled
                    }
                }

                MapEffect(Unit) { mapView ->
                    mapView.mapboxMap.loadStyle(Style.OUTDOORS) { style ->
                        Log.d(TAG, "Style loaded, adding WMS sources")

                        // Low zoom data
                        style.addSource(
                            rasterSource("snoskred-overview-source") {
                                tiles(listOf(WMS_OVERVIEW))
                                tileSize(256)
                                minzoom(4)
                                maxzoom(12)
                                attribution("© NVE")
                            }
                        )
                        style.addLayer(
                            rasterLayer("snoskred-overview-layer", "snoskred-overview-source") {
                                rasterOpacity(0.5)
                                maxZoom(11.5)
                            }
                        )

                        // High zoom data
                        style.addSource(
                            rasterSource("snoskred-detail-source") {
                                tiles(listOf(WMS_DETAIL))
                                tileSize(256)
                                minzoom(8)
                                maxzoom(18)
                                attribution("© NVE")
                            }
                        )
                        style.addLayer(
                            rasterLayer("snoskred-detail-layer", "snoskred-detail-source") {
                                rasterOpacity(0.9)
                                minZoom(8.0)
                            }
                        )
                    }
                }
                MapEffect(Unit) { mapView ->
                    idleSubscription = mapView.mapboxMap.subscribeMapIdle {
                        viewportState.cameraState?.toCameraOptions()?.let {
                            MapCameraRepository.lastCameraOptions = it
                        }
                    }
                }
            }
        }
    }
}