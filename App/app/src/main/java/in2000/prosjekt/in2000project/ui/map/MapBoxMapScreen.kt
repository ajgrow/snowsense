package in2000.prosjekt.in2000project.ui.map

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.generated.rasterSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.plugin.gestures.gestures

private const val TAG = "SnoskredWMS"

private const val WMS_BASE =
    "https://kart.nve.no/enterprise/services/SnoskredAktsomhet/MapServer/WMSServer"

private const val WMS_OVERVIEW =
    "$WMS_BASE?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap" +
            "&BBOX={bbox-epsg-3857}&CRS=EPSG:3857&WIDTH=256&HEIGHT=256" +
            "&LAYERS=PotensieltSkredfareOmr" +
            "&STYLES=&FORMAT=image/png&TRANSPARENT=TRUE"

private const val WMS_DETAIL =
    "$WMS_BASE?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap" +
            "&BBOX={bbox-epsg-3857}&CRS=EPSG:3857&WIDTH=256&HEIGHT=256" +
            "&LAYERS=S2_snoskred_m_skogeffekt_Aktsomhetsomrade" +
            "&STYLES=&FORMAT=image/png&TRANSPARENT=TRUE"

@Composable
fun MapboxMapScreen(
    geoJson: String?,
    onMapClick: () -> Unit
) {
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(10.0, 65.0))
            zoom(3.5)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState
        ) {
            MapEffect(Unit) { mapView ->
                mapView.gestures.addOnMapClickListener {
                    onMapClick()
                    true
                }
            }

            MapEffect(Unit) { mapView ->
                mapView.mapboxMap.loadStyle(Style.OUTDOORS) { style ->
                    Log.d(TAG, "Style loaded, adding WMS and GeoJSON sources")

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

                    style.addSource(
                        geoJsonSource("geojson-source") {
                            data("")
                        }
                    )

                    style.addLayer(
                        lineLayer("geojson-line-layer", "geojson-source") {
                            lineWidth(2.0)
                        }
                    )
                }
            }

            MapEffect(geoJson) { mapView ->
                if (!geoJson.isNullOrEmpty()) {
                    val style = mapView.mapboxMap.style ?: return@MapEffect
                    (style.getSource("geojson-source") as? GeoJsonSource)?.data(geoJson)
                }
            }
        }
    }
}