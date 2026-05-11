package in2000.prosjekt.in2000project.domain.repository

import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.DailyWeatherRecord
import java.time.LocalDate

/** Repository for recent weather records, used for slab avalanche risk assessment. */
interface WeatherRepository {
    /**
     * Constructs a list of weather records based on recent
     * weather observations from the nearest station.
     *
     * @param coordinate The map point to assess risk for (WGS-84).
     * @param currentDate The date to assess risk for (defaults to today).
     * @param lookBackDays The number of days to build the climate records
     * backwards from currentDate.
     * @return [List<DailyWeatherRecord>] consisting of daily weather records
     * for the selected range.
     * @throws java.io.IOException on network failures.
     * @throws in2000.prosjekt.in2000project.data.model.exceptions.NoObservationsException if the station has no data for the requested period.
     * @throws in2000.prosjekt.in2000project.data.model.exceptions.NoStationFoundException if no station is found for the requested coordinate.
     */
    suspend fun getWeatherRecordsForLocation(
        coordinate: Coordinate,
        currentDate: LocalDate = LocalDate.now(),
        lookBackDays: Int
    ): List<DailyWeatherRecord>
}