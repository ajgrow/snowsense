package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.datasource.dto.*
import in2000.prosjekt.in2000project.domain.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.domain.model.Coordinate

/** Fake that returns preset data and records arguments for verification. */
class FakeFrostV0DataSource : FrostV0DataSource {

    // ── Canned responses ──

    /** Override per test to control getNearestSource output. */
    var sourceResponse = makeSourceResponse(makeSource())

    /** Override per test to control getObservations output. */
    var observationResponse = makeObsResponse(
        makeDay(temp = 2.0, precip = 1.5, wind = 3.0, snow = 45.0),
    )

    /** Set to true to simulate network errors. */
    var shouldThrow = false

    // ── Argument capture ──

    /** The coordinate passed to the last getNearestSource call. */
    var lastCoordinate: Coordinate? = null
        private set

    /** The sourceId passed to the last getObservations call. */
    var lastSourceId: String? = null
        private set

    /** The elements CSV passed to the last getObservations call. */
    var lastElements: String? = null
        private set

    /** The referenceTime passed to the last getObservations call. */
    var lastReferenceTime: String? = null
        private set

    override suspend fun getNearestSource(
        coordinate: Coordinate,
    ): FrostV0SourceDto? {
        if (shouldThrow) throw java.io.IOException("Fake network error")
        lastCoordinate = coordinate
        return sourceResponse.data.firstOrNull()
    }

    override suspend fun getObservations(
        sourceId: String,
        elementsCsv: String,
        referenceTime: String,
    ): FrostV0ObservationResponseDto {
        if (shouldThrow) throw java.io.IOException("Fake network error")
        lastSourceId = sourceId
        lastElements = elementsCsv
        lastReferenceTime = referenceTime
        return observationResponse
    }
}
