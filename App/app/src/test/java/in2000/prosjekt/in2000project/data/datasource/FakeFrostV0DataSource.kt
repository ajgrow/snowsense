package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.datasource.dto.*
import in2000.prosjekt.in2000project.data.model.Coordinate

/** Fake that returns preset data and records arguments for verification. */
class FakeFrostV0DataSource : FrostV0DataSource {

    // ── Canned responses ──

    /** Override per test to control getNearestSource output. */
    var sourceResponse = FrostV0SourceResponse(
        data = listOf(
            FrostV0Source(
                id = "SN18700",
                name = "Blindern",
                shortName = "Blindern",
                geometry = FrostV0Point(
                    type = "Point",
                    coordinates = listOf(59.94, 10.72),
                ),
                distance = 1.2,
                masl = 94.0,
                validFrom = "1937-01-01",
                county = "Oslo",
                municipality = "Oslo",
            )
        )
    )

    /** Override per test to control getObservations output. */
    var observationResponse = FrostV0ObservationResponse(
        currentItemCount = 6,
        totalItemCount = 6,
        data = listOf(
            FrostV0ObservationsAtRefTime(
                sourceId = "SN18700",
                referenceTime = "2024-04-01T00:00:00.000Z",
                observations = listOf(
                    FrostV0Observation(
                        "mean(air_temperature P1D)",
                        -2.0,
                        "degC",
                        "PT0H",
                        "P1D",
                    ),
                    FrostV0Observation(
                        "sum(precipitation_amount P1D)",
                        4.5,
                        "mm",
                        "PT0H",
                        "P1D",
                    ),
                    FrostV0Observation(
                        "mean(wind_speed P1D)",
                        3.0,
                        "m/s",
                        "PT0H",
                        "P1D",
                    ),
                )
            ),
            FrostV0ObservationsAtRefTime(
                sourceId = "SN18700",
                referenceTime = "2025-04-01T00:00:00.000Z",
                observations = listOf(
                    FrostV0Observation(
                        "mean(air_temperature P1D)",
                        -4.0,
                        "degC",
                        "PT0H",
                        "P1D",
                    ),
                    FrostV0Observation(
                        "sum(precipitation_amount P1D)",
                        6.5,
                        "mm",
                        "PT0H",
                        "P1D",
                    ),
                    FrostV0Observation(
                        "mean(wind_speed P1D)",
                        5.0,
                        "m/s",
                        "PT0H",
                        "P1D",
                    ),
                )
            ),
        )
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
    ): FrostV0SourceResponse {
        if (shouldThrow) throw java.io.IOException("Fake network error")
        lastCoordinate = coordinate
        return sourceResponse
    }

    override suspend fun getObservations(
        sourceId: String,
        elements: String,
        referenceTime: String,
    ): FrostV0ObservationResponse {
        if (shouldThrow) throw java.io.IOException("Fake network error")
        lastSourceId = sourceId
        lastElements = elements
        lastReferenceTime = referenceTime
        return observationResponse
    }
}
