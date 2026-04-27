package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationResponse
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0SourceResponse
import in2000.prosjekt.in2000project.data.model.Coordinate
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorFrostV0DataSource(
    private val client: HttpClient,
    private val clientId: String
): FrostV0DataSource {
    override suspend fun getNearestSource(
        coordinate: Coordinate
    ): FrostV0SourceResponse {
        return client.get("https://frost.met.no/sources/v0.jsonld") {
            basicAuth(clientId, "")
            parameter("geometry",
                "nearest(POINT(${coordinate.longitude} ${coordinate.latitude}))")
            parameter("nearestmaxcount",
                "1")
        }.body()
    }

    override suspend fun getObservations(
        sourceId: String,
        elementsCsv: String,
        referenceTime: String
    ): FrostV0ObservationResponse {
        return client.get("https://frost.met.no/observations/v0.jsonld") {
            basicAuth(clientId, "")
            parameter("sources", sourceId)
            parameter("elements", elementsCsv)
            parameter("referencetime", referenceTime)
            parameter("timeoffsets", "default")
        }.body()
    }
}