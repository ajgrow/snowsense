package in2000.prosjekt.in2000project.data.repository


import in2000.prosjekt.in2000project.data.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0Observation
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0Source
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.LocationClimateData
import in2000.prosjekt.in2000project.data.model.LocationClimateDataPoint
import in2000.prosjekt.in2000project.data.model.NoObservationsException
import in2000.prosjekt.in2000project.data.model.NoStationFoundException
import java.time.LocalDate
import java.time.MonthDay
import in2000.prosjekt.in2000project.data.model.enums.ElementType
import in2000.prosjekt.in2000project.data.model.enums.MeasurementUnit

class FrostClimateRepository(
    private val frostV0DataSource: FrostV0DataSource
) : ClimateRepository {

    override suspend fun getClimateDataForLocation(
        coordinate: Coordinate,
        periodStart: MonthDay,
        periodEnd: MonthDay,
        yearsBack: Int,
    ): LocationClimateData {
        require(yearsBack >= 0)

        val station = getNearestStation(coordinate)

        // ISO-8601 repeating time range (R10/2014-03-01/2014-04-01/P1Y)
        val referenceTime = buildRepeatingTimeRange(
            periodStart,
            periodEnd,
            yearsBack,
        )

        val obsResponse = frostV0DataSource.getObservations(
            station.id,
            elementsCsv,
            referenceTime,
        )

        val allObservations = obsResponse.data.flatMap { it.observations }

        if (allObservations.isEmpty()) {
            throw NoObservationsException(
                stationName = station.name,
                message = "Station ${station.name} has no observations for " +
                        "$periodStart–$periodEnd over the last $yearsBack years",
            )
        }

        // Group by year, calculate averages for each element, create data points
        val yearlyAverages = obsResponse.data
            .groupBy { it.referenceTime.take(4).toIntOrNull() ?: 0 }
            .flatMap { (year, yearData) ->
                val yearObservations = yearData.flatMap { it.observations }
                val averages = averageByElement(yearObservations)
                toYearlyDataPoints(year, averages)
            }

        return LocationClimateData(
            coordinate = coordinate,
            stationName = station.name,
            stationDistanceKm = station.distance,
            stationElevationMasl = station.masl,
            periodStart = periodStart,
            periodEnd = periodEnd,
            yearsBack = yearsBack,
            dataPointCount = allObservations.size,
            yearlyAverages = yearlyAverages
        )
    }

    // Throws NoStationFoundException if no station found.
    private suspend fun getNearestStation(
        coordinate: Coordinate
    ): FrostV0Source = frostV0DataSource
        .getNearestSource(coordinate)
        .data
        .firstOrNull()
        ?: throw NoStationFoundException(coordinate)

    // Pair years and ElementType averages, skipping missing elements.
    private fun toYearlyDataPoints(
        year: Int,
        averages: Map<String, Double>,
    ): List<LocationClimateDataPoint>
    = ElementType.entries.mapNotNull { type ->
        averages[type.frostElementId]?.let { avgValue ->
            LocationClimateDataPoint( year,  type,  type.defaultUnit,  avgValue)
        }
    }
}

private val ElementType.frostElementId: String
    get() = when (this) {
        ElementType.TEMPERATURE_MEAN_P1D -> "mean(air_temperature P1D)"
        ElementType.PRECIPITATION_SUM_P1D -> "sum(precipitation_amount P1D)"
        ElementType.WIND_SPEED_MEAN_P1D -> "mean(wind_speed P1D)"
        ElementType.SNOW_DEPTH_MAX_P1D -> "max(surface_snow_thickness P1D)"
    }

private val ElementType.defaultUnit: MeasurementUnit
    get() = when (this) {
        ElementType.TEMPERATURE_MEAN_P1D -> MeasurementUnit.CELSIUS
        ElementType.PRECIPITATION_SUM_P1D -> MeasurementUnit.MILLIMETERS
        ElementType.WIND_SPEED_MEAN_P1D -> MeasurementUnit.METERS_PER_SECOND
        ElementType.SNOW_DEPTH_MAX_P1D -> MeasurementUnit.CENTIMETERS
    }

private val elementsCsv: String =
    ElementType.entries.joinToString(",") { it.frostElementId }

    /**
     * Builds a Frost v0 repeating-interval referenceTime string.
     *
     * Returns e.g. "R10/2017-03-23/2017-04-23/P1Y" which tells Frost:
     * "give me data for Mar 23–Apr 22 (inclusive), repeated yearly for 10 years."
     *
     * Handles year-boundary crossings (e.g. Dec 15 – Jan 15).
     *
     * @param periodStart First day of the window (inclusive).
     * @param periodEnd Last day of the window (inclusive). +1 day added internally for Frost's half-open interval.
     * @param yearsBack How many years back to include observations from.
     * @param referenceDate Used to determine which years to query. Pass explicitly in tests for deterministic results.
     */
    internal fun buildRepeatingTimeRange(
        periodStart: MonthDay,
        periodEnd: MonthDay,
        yearsBack: Int,
        referenceDate: LocalDate = LocalDate.now(),
    ): String {
        require(yearsBack >= 0)

        val oldestStartYear = referenceDate.year - yearsBack

        val firstStart = periodStart.atYear(oldestStartYear)

        val crossesYearBoundary = periodStart.isAfter(periodEnd)

        val endYear = if (crossesYearBoundary) oldestStartYear + 1
        else oldestStartYear

        val firstEnd = periodEnd.atYear(endYear).plusDays(1)

        return "R${yearsBack + 1}/$firstStart/$firstEnd/P1Y"
    }

    /** Mean observation value per Frost elementId. */
    internal fun averageByElement(
        observations: List<FrostV0Observation>
    ): Map<String, Double>
    = observations
        .groupBy { it.elementId }
        .mapValues { groupedObservations ->
            groupedObservations.value
                .map { observation -> observation.value }
                .average()
        }
