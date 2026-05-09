package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.DailyWeatherRecord
import in2000.prosjekt.in2000project.data.model.LocationSlabAvalancheRiskData
import in2000.prosjekt.in2000project.data.model.NoObservationsException
import in2000.prosjekt.in2000project.data.model.NoStationFoundException
import java.time.LocalDate

/** Repository for avalanche risk assessments based on recent climate data. */
interface AvalancheRiskRepository {
    /**
     * Assesses slab avalanche risk for the given location based on recent
     * weather observations from the nearest station.
     *
     * @param coordinate The map point to assess risk for (WGS-84).
     * @param currentDate The date to assess risk for (defaults to today).
     * @param lookBackDays The number of days to look back in the climate records.
     * @return [LocationSlabAvalancheRiskData] with the assessed danger level.
     * @throws java.io.IOException on network failures.
     * @throws NoObservationsException if the station has no data for the requested period.
     * @throws NoStationFoundException if no station is found for the requested coordinate.
     */
    suspend fun getWeatherRecordsForLocation(
        coordinate: Coordinate,
        currentDate: LocalDate = LocalDate.now(),
        lookBackDays: Int
    ): List<DailyWeatherRecord>
}