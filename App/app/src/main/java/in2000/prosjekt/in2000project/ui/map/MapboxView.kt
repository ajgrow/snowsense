package in2000.prosjekt.in2000project.ui.map

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    modifier: Modifier = Modifier,
    searchState: SearchUiState,
    onSearch: (String) -> Unit,
    onSelectResult: (NamedLocation) -> Unit
) {

    val (hasSearch, searchLng, searchLat) = remember(coordinates) {
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

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var isSearchActive by remember { mutableStateOf(false) }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    var idleSubscription by remember { mutableStateOf<Cancelable?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            viewportState.cameraState?.toCameraOptions()?.let {
                MapCameraRepository.updateCameraOptions(it)
            }
            idleSubscription?.cancel()
        }
    }

    var addPin by remember { mutableStateOf<((Point) -> Unit)?>(null) }

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
                    MapCameraRepository.lastCameraOptions?.let {
                        mapView.mapboxMap.setCamera(it)
                    }


                    val annotationApi = mapView.annotations
                    val pointAnnotationManager: PointAnnotationManager = annotationApi.createPointAnnotationManager()

                    fun localAddPin(point: Point){
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

                    addPin = ::localAddPin

                    //this click listener just works generally on the map
                    mapView.gestures.addOnMapClickListener { point ->
                        if (!isSearchActive) {
                            addPin?.invoke(point)
                            onMapClick(point)
                        }

                        isSearchActive = false
                        focusManager.clearFocus()

                        true
                    }

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




                    if (hasSearch && searchLat != null && searchLng != null){
                        val searchPoint = Point.fromLngLat(searchLng, searchLat)

                        mapView.mapboxMap.setCamera(
                            CameraOptions
                                .Builder()
                                .center(searchPoint)
                                .zoom(12.0)
                                .build())
                        addPin?.invoke(searchPoint)
                    }
                }
            }

            val mapButtonModifier = Modifier.size(50.dp)

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = { isSearchActive = true },
                    modifier = mapButtonModifier,
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isSearchActive
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Open Search")
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    modifier = mapButtonModifier,
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
                        text = "+",
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    modifier = mapButtonModifier,
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
                        text = "-",
                        textAlign = TextAlign.Center
                    )
                }

            }

            val onNavigateToMap: (Coordinate) -> Unit = { coordinate ->
                val point = Point.fromLngLat(coordinate.longitude, coordinate.latitude)
                viewportState.easeTo(
                    cameraOptions {
                        center(point)
                        zoom(12.0)
                    }
                )
                addPin?.invoke(point)
            }

            if (isSearchActive) {
                SearchSection(
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .padding(top = 24.dp),
                    textFieldState = searchState.searchTextFieldState,
                    onSearch = onSearch,
                    searchResults = searchState.searchResults,
                    onNavigateToMap = { coordinate ->
                        onNavigateToMap(coordinate)
                        isSearchActive = false
                        focusManager.clearFocus()
                    },
                    onSelectResult = onSelectResult,
                    recentSearches = searchState.recentSearches
                )
            }
        }
    }
}

