package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.model.GeoJson
import in2000.prosjekt.in2000project.network.KtorClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.collections.emptyList

class NveDataSource {

    private val httpClient = KtorClient.client

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

                    parameter("distance", buffer)
                    parameter("units", "esriSRUnit_Meter")

                    parameter("outFields", "objekttype,skredtype")
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
