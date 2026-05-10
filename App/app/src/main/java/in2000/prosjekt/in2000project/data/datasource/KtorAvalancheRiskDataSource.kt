package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.datasource.dto.GeoJsonResponseDto
import in2000.prosjekt.in2000project.data.datasource.AvalancheRiskDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorAvalancheRiskDataSource(
    private val client: HttpClient
) : AvalancheRiskDataSource {
    override suspend fun getGeoJson(lat: Double, lon: Double, buffer: Int): GeoJsonResponseDto {
        return try {
            client
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
                .body<GeoJsonResponseDto>()
        } catch (e: Exception){
            e.printStackTrace()
            GeoJsonResponseDto(
                features = emptyList(),
                type = "FeatureCollection")
        }
    }
}
