package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationResponse
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0Source
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0SourceResponse
import in2000.prosjekt.in2000project.data.model.Coordinate

/**
 * Data source for MET Norway's Frost API (frost.met.no).
 * Provides access to weather station metadata and historical observations.
 */
interface FrostV0DataSource {

    /**
     * Finds the nearest weather station to the given coordinates.
     *
     * @param coordinate The point to search from (WGS-84).
     * @return [FrostV0Source] The nearest station with distance in km.
     * @throws java.io.IOException on network or I/O failures.
     */
    suspend fun getNearestSource(
        coordinate: Coordinate
    ): FrostV0Source?

    /**
     * Fetches weather observations from a station.
     *
     * @param sourceId Station identifier, e.g. `"SN18700"` (Blindern).
     * @param elementsCsv Comma-separated element IDs, e.g. `"mean(air_temperature P1D)"`.
     *   See [element table](https://frost.met.no/elementtable) for valid IDs.
     * @param referenceTime ISO 8601 time range (half-open), e.g. `"2020-01-01/2025-01-01"`.
     *   Also supports `"latest"` for the most recent data.
     * @return [FrostV0ObservationResponse] containing the observation data.
     * @throws java.io.IOException on network or I/O failures.
     */
    suspend fun getObservations(
        sourceId: String,
        elementsCsv: String,
        referenceTime: String,
    ): FrostV0ObservationResponse
}