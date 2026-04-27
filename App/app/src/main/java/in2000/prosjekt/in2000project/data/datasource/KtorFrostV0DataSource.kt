package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationResponse
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0Source
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0SourceResponse
import in2000.prosjekt.in2000project.data.model.Coordinate
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorFrostV0DataSource(
    private val client: HttpClient
): FrostV0DataSource {
    override suspend fun getNearestSource(
        coordinate: Coordinate
    ): FrostV0Source?
    = client.get("https://frost.met.no/sources/v0.jsonld") {
            parameter("geometry",
                "nearest(POINT(${coordinate.longitude} ${coordinate.latitude}))")
            parameter("nearestmaxcount",
                "1")
        }.body<FrostV0SourceResponse>()
        .data
        .firstOrNull()

    override suspend fun getObservations(
        sourceId: String,
        elementsCsv: String,
        referenceTime: String
    ): FrostV0ObservationResponse
    = client.get("https://frost.met.no/observations/v0.jsonld") {
            parameter("sources", sourceId)
            parameter("elements", elementsCsv)
            parameter("referencetime", referenceTime)
            parameter("timeoffsets", "default")
        }.body()
}