package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.LocationClimateData

/**
 * Repository for accessing historical climate data.
 *
 * Abstracts the data source(s) so that the domain/UI layers
 * remain independent of network or local storage details.
 */
interface ClimateRepository {

    /**
     * Returns aggregated climate data for the given coordinates.
     *
     * @param coordinate The map point to fetch climate data for (WGS-84).
     * @return [LocationClimateData] containing the climate observations for the location.
     * @throws java.io.IOException on network or I/O failures.
     */
    suspend fun getClimateDataForLocation(
        coordinate: Coordinate
    ): LocationClimateData
}