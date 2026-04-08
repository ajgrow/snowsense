package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.model.GeoJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import java.net.URL
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.collections.emptyList

class NveDataSource {

    private val httpClient = HttpClient(Android){
        install(ContentNegotiation){
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getGeoJson(lat: Double, lon: Double, buffer: Int): GeoJson {
        return try {
            httpClient
                .get("https://kart.nve.no/enterprise/rest/services/SnoskredAktsomhet/MapServer/3/query"){
                    parameter("where", "1=1")

                    parameter(
                        "geometry",
                        """{"x":$lon, "y":$lat, "spatialReference":{"wkid":4326}}"""
                    )

                    parameter("geometryType", "esriGeometryPoint")
                    parameter("inSR", "4326")
                    parameter("spatialRel", "esriSpatialRelIntersects")

                    parameter("distance", buffer.toString())
                    parameter("units", "esriSRUnit_Meter")

                    parameter("outFields", "objekttype,skredstype")
                    parameter("returnGeometry", "true")
                    parameter("outSR", "4326")
                    parameter("f", "geojson")
                }
                .body<GeoJson>()
        } catch (e: Exception){
            e.printStackTrace()
            GeoJson(
                features = emptyList(),
                type = "FeatureCollection")
        }
    }
}
