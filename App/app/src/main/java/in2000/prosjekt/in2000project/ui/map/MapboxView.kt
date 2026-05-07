package in2000.prosjekt.in2000project.ui.map

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.toCameraOptions
import in2000.prosjekt.in2000project.ui.settings.SettingsViewModel
import in2000.prosjekt.in2000project.ui.settings.ThemeMode
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.NamedLocation
import in2000.prosjekt.in2000project.ui.reusables.SearchSection
import in2000.prosjekt.in2000project.ui.search.SearchUiState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.Projection
import com.mapbox.maps.extension.style.projection.generated.setProjection
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.attribution.generated.AttributionSettings
import com.mapbox.maps.plugin.compass.generated.CompassSettings
import com.mapbox.maps.plugin.logo.generated.LogoSettings
import com.mapbox.maps.plugin.scalebar.generated.ScaleBarSettings
import in2000.prosjekt.in2000project.ui.search.SearchViewModel

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
    mapStateHolder: MapStateHolder,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var mapViewState by remember {
        mutableStateOf<MapView?>(null)
    }

    val viewportState = rememberMapViewportState {
        val savedCamera = mapStateHolder.cameraOptions.value
        val pin = mapStateHolder.pinnedPoint.value

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

    var pinManager by remember {
        mutableStateOf<PointAnnotationManager?>(null)
    }
    var currentPin by remember {
        mutableStateOf<PointAnnotation?>(null) }

    val pinPoint by mapStateHolder.pinnedPoint.collectAsStateWithLifecycle()

    val animationOptions = MapAnimationOptions.Builder().duration(300).build()

    val density = LocalDensity.current
    val statusBarHeightPx = with(density) {
        contentPadding.calculateTopPadding().toPx()
    }
    val bottomPaddingPx = with(density) {
        contentPadding.calculateBottomPadding().toPx()
    }

    LaunchedEffect(Unit) {
        mapStateHolder.commands.collect { cmd ->
            val padding = EdgeInsets(
                0.0,
                0.0,
                mapStateHolder.bottomPaddingPx.value,
                0.0)

            when (cmd) {
                is MapStateHolder.MapCommand.FlyTo ->
                    viewportState.easeTo(
                        cameraOptions {
                            center(cmd.point)
                            zoom(cmd.zoom)
                            padding(padding)
                        },
                        animationOptions
                    )
                is MapStateHolder.MapCommand.Recenter -> {
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
                is MapStateHolder.MapCommand.ZoomBy -> {
                    val current = viewportState.cameraState?.zoom ?: 10.0
                    viewportState.easeTo(
                        cameraOptions { zoom(current + cmd.delta) },
                        animationOptions
                    )
                }
            }
        }
    }

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = viewportState,
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
    ){
        MapEffect(Unit) { mapView ->

            val mapboxMap = mapView.mapboxMap

            mapboxMap.getStyle { style ->
                style.setProjection(
                    Projection(ProjectionName.MERCATOR)
                )
            }

            mapViewState = mapView

            mapboxMap.setBounds()

            mapboxMap.loadStyle(
                Style.OUTDOORS,
                Style::addNveAvalancheRiskLayers)

            mapView.gestures.addOnMapClickListener { point ->
                mapStateHolder.placePin(point)
                true
            }

            pinManager = mapView.annotations
                .createPointAnnotationManager()

            mapStateHolder.pinnedPoint.value?.let { pin ->
                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(pin)
                        .zoom(mapStateHolder.cameraOptions.value?.zoom ?: 10.0)
                        .build()
                )
            }

            idleSubscription = mapView.mapboxMap
                .subscribeMapIdle {
                    mapStateHolder.onCameraIdle(
                        mapView.mapboxMap
                            .cameraState.toCameraOptions())
                }

            mapView.mapboxMap.subscribeMapLoadingError { error ->
                Log.e(TAG, "Map loading error: ${error.message}")
                mapStateHolder.onOverlayError()
            }
        }

        LaunchedEffect(pinPoint, pinManager) {
            val currentPinManager = pinManager
                ?: return@LaunchedEffect

            val point = pinPoint

            if (point == null) {
                currentPin?.let { pointAnnotation ->
                    currentPinManager.delete(pointAnnotation)
                }

                currentPin = null

                return@LaunchedEffect
            }

            val existing = currentPin
            if(existing == null) {
                val bitmap = BitmapFactory.decodeResource(
                    mapViewState?.context?.resources
                        ?: return@LaunchedEffect,
                    R.drawable.red_marker
                )

                currentPin = currentPinManager
                    .create(PointAnnotationOptions()
                        .withPoint(point)
                        .withIconImage(bitmap)
                        .withIconSize(0.3)
                    )
            } else {
                existing.point = point
                currentPinManager.update(existing)
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

