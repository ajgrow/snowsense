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
import java.time.Year
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

        // 1. Find the nearest weather station that can provide data.
        val station = getNearestStation(coordinate)

        // 2. Build the ISO-8601 repeating time range string (e.g., R10/2014-03-01/2014-04-01/P1Y).
        val referenceTime = buildRepeatingTimeRange(
            periodStart,
            periodEnd,
            yearsBack,
        )

        // 3. Fetch observations for the specified elements and time range.
        val obsResponse = frostV0DataSource.getObservations(
            station.id,
            elementsCsv,
            referenceTime,
        )

        // 4. Flatten all nested observations to check if any data was returned at all.
        val allObservations = obsResponse.data.flatMap { it.observations }

        if (allObservations.isEmpty()) {
            throw NoObservationsException(
                stationName = station.name,
                message = "Station ${station.name} has no observations for " +
                        "$periodStart–$periodEnd over the last $yearsBack years",
            )
        }

        // 5. Process the data year-by-year to calculate averages for each element.
        val yearlyAverages = obsResponse.data
            .groupBy { it.referenceTime.take(4).toIntOrNull() ?: 0 }
            .flatMap { (year, yearData) ->
                val yearObservations = yearData.flatMap { it.observations }
                val averages = averageByElement(yearObservations)
                toYearlyDataPoints(year, averages)
            }

        // 6. Map the processed results into the final domain model.
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

    /** Returns the nearest station to [coordinate], or throws [NoStationFoundException]. */
    private suspend fun getNearestStation(
        coordinate: Coordinate
    ): FrostV0Source = frostV0DataSource
        .getNearestSource(coordinate)
        .data
        .firstOrNull()
        ?: throw NoStationFoundException(coordinate)


    /**
     * Converts a year's averaged observations into [LocationClimateDataPoint]s.
     *
     * Uses [frostElementId] to look up each element's average, and [defaultUnit]
     * to assign the correct measurement unit. Elements not present in [averages]
     * are omitted from the result.
     */
    private fun toYearlyDataPoints(
        year: Int,
        averages: Map<String, Double>,
    ): List<LocationClimateDataPoint> = ElementType.entries.mapNotNull { type ->
        averages[type.frostElementId]?.let { avgValue ->
            LocationClimateDataPoint(
                year,
                type,
                type.defaultUnit,
                avgValue,
            )
        }
    }
}

/** The Frost v0 element-id string for each [ElementType]. */
private val ElementType.frostElementId: String
    get() = when (this) {
        ElementType.TEMPERATURE_MEAN_P1D -> "mean(air_temperature P1D)"
        ElementType.PRECIPITATION_SUM_P1D -> "sum(precipitation_amount P1D)"
        ElementType.WIND_SPEED_MEAN_P1D -> "mean(wind_speed P1D)"
        ElementType.SNOW_DEPTH_MAX_P1D -> "max(surface_snow_thickness P1D)"
    }

/** The default measurement unit for each [ElementType]. */
private val ElementType.defaultUnit: MeasurementUnit
    get() = when (this) {
        ElementType.TEMPERATURE_MEAN_P1D -> MeasurementUnit.CELSIUS
        ElementType.PRECIPITATION_SUM_P1D -> MeasurementUnit.MILLIMETERS
        ElementType.WIND_SPEED_MEAN_P1D -> MeasurementUnit.METERS_PER_SECOND
        ElementType.SNOW_DEPTH_MAX_P1D -> MeasurementUnit.CENTIMETERS
    }

/** Comma-separated Frost v0 element IDs for all supported element types. */
private val elementsCsv: String =
    ElementType.entries.joinToString(",") { it.frostElementId }

// --- Helper functions (top-level for independent testability) ---

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

        val firstStart = periodStart.atYearClamped(oldestStartYear)

        val crossesYearBoundary = periodStart.isAfter(periodEnd)

        val endYear = if (crossesYearBoundary) oldestStartYear + 1
        else oldestStartYear

        val firstEnd = periodEnd.atYearClamped(endYear).plusDays(1)

        return "R${yearsBack + 1}/$firstStart/$firstEnd/P1Y"
    }

    /**
     * Like [MonthDay.atYear] but clamps to the last valid day of the month
     *  instead of throwing on e.g. Feb 29 in a non-leap year.
     */
    private fun MonthDay.atYearClamped(
        year: Int
    ): LocalDate = LocalDate.of(
        year,
        month,
        minOf(
            dayOfMonth,
            month.length(Year.isLeap(year.toLong()))))
    /**
     * Groups daily observations by elementId and computes the flat mean for each
     * element. Elements with no observations are absent from the returned map.
     */
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
