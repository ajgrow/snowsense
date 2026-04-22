package in2000.prosjekt.in2000project.ui.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapbox.common.Cancelable
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.rasterSource
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.toCameraOptions
import in2000.prosjekt.in2000project.R

/*
This global object is bad practice but it works and I
have not found any other practical working alternatives.
*/
object MapCameraRepository {
    var lastCameraOptions: CameraOptions? = null
        private set

    fun updateCameraOptions(options: CameraOptions) {
        lastCameraOptions = options
    }
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
            "&LAYERS=S2_snoskred_m_skogeffekt_Aktsomhetsomrade" +
            "&STYLES=&FORMAT=image/png&TRANSPARENT=TRUE"

@Composable
fun MapboxView(
    onMapClick: (Point) -> Unit,
    coordinates: String,
    onOverlayError: () -> Unit,
    onOverlayLoaded: () -> Unit,
    modifier: Modifier = Modifier
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
                .also { MapCameraRepository.updateCameraOptions(it) }
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


    val mapRecreateKey = remember(hasSearch, searchLat, searchLng) {
        if (hasSearch) System.currentTimeMillis() else "default"
    }


    DisposableEffect(Unit) {
        onDispose {
            viewportState.cameraState?.toCameraOptions()?.let {
                MapCameraRepository.updateCameraOptions(it)
            }
        }
    }

    key(mapRecreateKey) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            MapboxMap(
                modifier = modifier,
                mapViewportState = viewportState
            ) {
                MapEffect(Unit) { mapView ->
                    mapView.mapboxMap.subscribeMapLoadingError { error ->
                        Log.e(TAG, "Map loading error: ${error.message}")
                        onOverlayError()
                    }

                    mapView.mapboxMap.loadStyle(Style.OUTDOORS) { style ->
                        onOverlayLoaded()
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
                                rasterHueRotate(160.0)
                            }
                        )
                    }

                    var idleSubscription: Cancelable? = null
                    idleSubscription = mapView.mapboxMap.subscribeMapIdle {
                        viewportState.cameraState?.toCameraOptions()?.let {
                            MapCameraRepository.updateCameraOptions(it)
                        }
                    }

                    val mapBoxMap = mapView.mapboxMap

                    val bounds = CoordinateBounds(
                        Point.fromLngLat(2.5, 55.9),
                        Point.fromLngLat(31.5, 71.5)
                    )

                    mapBoxMap.setBounds(
                        CameraBoundsOptions.Builder()
                            .bounds(bounds)
                            .minZoom(3.5)
                            .maxZoom(18.0)
                            .build()
                    )

                    val annotationApi = mapView.annotations
                    val pointAnnotationManager: PointAnnotationManager = annotationApi.createPointAnnotationManager()

                    fun addPin(point: Point){
                        pointAnnotationManager.deleteAll()
                        val bitmap = BitmapFactory.decodeResource(
                            mapView.context.resources,
                            R.drawable.red_marker
                        )

                        val pointAnnotationOptions = PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage(bitmap)
                            .withIconSize(0.3)
                        pointAnnotationManager.create(pointAnnotationOptions)
                    }

                    mapView.gestures.addOnMapClickListener { point ->
                        addPin(point)
                        onMapClick(point)
                        true
                    }

                    if (hasSearch && searchLat != null && searchLng != null){
                        val searchPoint = Point.fromLngLat(searchLng, searchLat)

                        mapView.mapboxMap.setCamera(
                            CameraOptions
                                .Builder()
                                .center(searchPoint)
                                .zoom(12.0)
                                .build())
                        addPin(searchPoint)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val currentZoom = viewportState
                            .cameraState?.zoom ?: 10.0
                        viewportState.easeTo(
                            cameraOptions {
                                zoom(currentZoom + 1.0)
                            },
                            MapAnimationOptions.Builder()
                                .duration(300)
                                .build()
                        )
                    }
                ) {
                    Text(
                        text = "+"
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        val currentZoom = viewportState
                            .cameraState?.zoom ?: 10.0
                        viewportState.easeTo(
                            cameraOptions {
                                zoom(currentZoom - 1.0)
                            },
                            MapAnimationOptions.Builder()
                                .duration(300)
                                .build()
                        )
                    }
                ) {
                    Text(
                        text = "-"
                    )
                }
            }
        }
    }
}

