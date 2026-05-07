package in2000.prosjekt.in2000project.ui.map

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.toCameraOptions
import in2000.prosjekt.in2000project.R
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.Projection
import com.mapbox.maps.extension.style.projection.generated.setProjection
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation

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
    mapViewModel: MapViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var mapViewState by remember {
        mutableStateOf<MapView?>(null)
    }

    val mapScreenState by mapViewModel.screenState.collectAsState()

    val viewportState = rememberMapViewportState {
        val savedCamera = mapScreenState.cameraOptions
        val pin = mapScreenState.pinnedPoint

        setCameraOptions(
            when {
                pin != null && savedCamera != null
                    -> CameraOptions.Builder()
                    .center(pin)
                    .zoom(savedCamera.zoom ?: 10.0)
                    .bearing(savedCamera.bearing)
                    .pitch(savedCamera.pitch)
                    .build()

                savedCamera != null
                    -> savedCamera

                pin != null
                    -> CameraOptions.Builder()
                    .center(pin)
                    .zoom(10.0)
                    .build()


                else
                    -> CameraOptions.Builder()
                    .center(Point.fromLngLat(15.0, 65.0))
                    .bearing(16.0)
                    .zoom(4.1)
                    .build()
            }
        )
    }

    var idleSubscription by remember {
        mutableStateOf<Cancelable?>(null) }

    DisposableEffect(Unit) {
        onDispose { idleSubscription?.cancel() }
    }

    val pinPoint = mapScreenState.pinnedPoint

    val animationOptions = MapAnimationOptions.Builder().duration(300).build()

    val density = LocalDensity.current
    val statusBarHeightPx = with(density) {
        contentPadding.calculateTopPadding().toPx()
    }
    LaunchedEffect(mapViewModel) {
        mapViewModel.mapActions.collect { cmd ->
            val padding = EdgeInsets(
                0.0,
                0.0,
                mapScreenState.bottomPaddingPx,
                0.0)

            when (cmd) {
                is MapViewModel.MapAction.FlyTo ->
                    viewportState.easeTo(
                        cameraOptions {
                            center(cmd.point)
                            zoom(cmd.zoom)
                            padding(padding)
                        },
                        animationOptions
                    )
                is MapViewModel.MapAction.Recenter -> {
                    val currentZoom = viewportState.cameraState?.zoom
                    viewportState.easeTo(
                        cameraOptions {
                            center(cmd.point)
                            currentZoom?.let { zoom(it) }
                            padding(padding)
                        },
                        animationOptions
                    )
                }
                is MapViewModel.MapAction.ZoomBy -> {
                    val current = viewportState.cameraState?.zoom ?: 10.0
                    viewportState.easeTo(
                        cameraOptions { zoom(current + cmd.delta) },
                        animationOptions
                    )
                }
            }
        }
    }

    val context = LocalContext.current

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = viewportState,
        onMapClickListener = { point ->
            mapViewModel.onMapClick(point.latitude(), point.longitude())
            true
        },
        compass = {
            Compass(
                modifier = Modifier
                    .padding(top = with(LocalDensity.current) { statusBarHeightPx.toDp() + 40.dp })
            )
        },
        scaleBar = {
            ScaleBar(
                modifier = Modifier
                    .padding(top = with(LocalDensity.current) { statusBarHeightPx.toDp() + 40.dp })
            )
        }
    ) {
        MapEffect(Unit) { mapView ->
            val mapboxMap = mapView.mapboxMap
            mapboxMap.getStyle { style ->
                style.setProjection(Projection(ProjectionName.MERCATOR))
            }
            mapboxMap.setBounds()
            mapboxMap.loadStyle(Style.OUTDOORS, Style::addNveAvalancheRiskLayers)

            mapView.mapboxMap.subscribeMapIdle {
                mapViewModel.onCameraIdle(mapView.mapboxMap.cameraState.toCameraOptions())
            }
        }

        pinPoint?.let { point ->
            PointAnnotation(
                point = point
            ) {
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.red_marker)
                iconImage = IconImage(bitmap)
                iconSize = 0.3
            }
        }
    }
}

private fun Style.addNveAvalancheRiskLayers() {
    Log.d(TAG, "Style loaded, adding WMS sources")

    // Low zoom data
    addSource(
        rasterSource(
            "snoskred-overview-source"
        ) {
            tiles(listOf(WMS_OVERVIEW))
            tileSize(256)
            minzoom(6)
            maxzoom(12)
            attribution("© NVE")
        }
    )
    addLayer(
        rasterLayer(
            "snoskred-overview-layer",
            "snoskred-overview-source"
        ) {
            rasterOpacity(0.5)
            minZoom(8.0)
            maxZoom(12.0)
            rasterFadeDuration(400.0)
        }
    )

    // High zoom data
    addSource(
        rasterSource(
            "snoskred-detail-source"
        ) {
            tiles(listOf(WMS_DETAIL))
            tileSize(256)
            minzoom(11)
            maxzoom(24)
            attribution("© NVE")
        }
    )
    addLayer(
        rasterLayer(
            "snoskred-detail-layer",
            "snoskred-detail-source"
        ) {
            rasterOpacity(0.7)
            minZoom(12.0)
            rasterHueRotate(160.0)
            rasterFadeDuration(200.0)
        }
    )
}

private fun MapboxMap.setBounds() {
    val bounds = CoordinateBounds(
        Point.fromLngLat(-5.0, 48.0),
        Point.fromLngLat(40.0, 75.0)
    )

    setBounds(
        CameraBoundsOptions.Builder()
            .bounds(bounds)
            .minZoom(3.5)
            .maxZoom(18.0)
            .build()
    )
}

