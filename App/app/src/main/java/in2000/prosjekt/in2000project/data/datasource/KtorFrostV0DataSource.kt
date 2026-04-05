package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationResponse
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0SourceResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorFrostV0DataSource(
    private val client: HttpClient
): FrostV0DataSource {
    override suspend fun getNearestSource(
        latitude: Double,
        longitude: Double
    ): FrostV0SourceResponse {
        return client.get("https://frost.met.no/sources/v0.jsonld") {
            parameter("geometry", "nearest(POINT($longitude $latitude))")
            parameter("nearestmaxcount", "1")
        }.body()
    }

    override suspend fun getObservations(
        sourceId: String,
        elements: String,
        referenceTime: String
    ): FrostV0ObservationResponse {
        return client.get("https://frost.met.no/observations/v0.jsonld") {
            parameter("sources", sourceId)
            parameter("elements", elements)
            parameter("referencetime", referenceTime)
            parameter("timeoffsets", "default")
        }.body()
    }
}