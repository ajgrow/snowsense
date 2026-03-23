package in2000.prosjekt.in2000project.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import in2000.prosjekt.in2000project.repository.GeoJsonRepository

@Composable
fun MapboxMapScreen() {
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(10.0, 65.0))
            zoom(3.5)
        }
    }

    // State to hold GeoJSON
    var geoJson by remember { mutableStateOf<String?>(null) }

    // Fetch data once
    LaunchedEffect(Unit) {
        geoJson = withContext(Dispatchers.IO) {
            GeoJsonRepository.fetchGeoJson()
        }
    }

    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapViewportState = viewportState
    ) {
        MapEffect(geoJson) { mapView ->

            val data = geoJson ?: return@MapEffect
            val mapboxMap = mapView.mapboxMap

            mapboxMap.getStyle { style ->

                val source = geoJsonSource("nve-source") {
                    data(data)
                }
                style.addSource(source)

                val fillLayer = fillLayer("nve-fill", "nve-source") {
                    fillColor("#ff0000")
                    fillOpacity(0.8)
                }
                style.addLayer(fillLayer)

                val lineLayer = lineLayer("nve-outline", "nve-source") {
                    lineColor("#ff0000")
                    lineWidth(2.5)
                }
                style.addLayer(lineLayer)
            }
        }
    }
}