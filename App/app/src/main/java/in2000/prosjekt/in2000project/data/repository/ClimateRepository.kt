package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.LocationClimateData
import in2000.prosjekt.in2000project.data.model.exceptions.NoObservationsException
import in2000.prosjekt.in2000project.data.model.exceptions.NoStationFoundException
import java.time.MonthDay

/**
 * Repository for accessing historical climate data.
 * Abstracts the data source(s) so that the UI layer remains independent of network details.
 */
interface ClimateRepository {

    /**
     * Returns historical daily climate averages for the given location and period.
     *
     * Queries the same calendar period ([periodStart] to [periodEnd]) across [yearsBack] years
     * and computes the mean of all daily observations per element.
     *
     * @param coordinate The map point to fetch climate data for (WGS-84).
     * @param periodStart First day of the period (inclusive), e.g. March 23.
     * @param periodEnd Last day of the period (inclusive), e.g. April 22.
     * @param yearsBack Number of additional years to include periods from (e.g. 9 → queries 2017–2026 if current year is 2026).
     * @return [LocationClimateData] with averaged daily values and metadata.
     * @throws java.io.IOException on network failures.
     * @throws NoObservationsException if the station has no data for the requested period.
     * @throws NoStationFoundException if no station is found for the requested coordinate.
     */
    suspend fun getClimateDataForLocation(
        coordinate: Coordinate,
        periodStart: MonthDay,
        periodEnd: MonthDay,
        yearsBack: Int,
    ): LocationClimateData?
}