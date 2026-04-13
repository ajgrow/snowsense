package in2000.prosjekt.in2000project.data.repository


import in2000.prosjekt.in2000project.data.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0Observation
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0Source
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.LocationClimateData
import in2000.prosjekt.in2000project.data.model.LocationSlabAvalancheRiskData
import in2000.prosjekt.in2000project.data.model.NoObservationsException
import in2000.prosjekt.in2000project.data.model.NoStationFoundException
import java.time.LocalDate
import java.time.MonthDay
import java.time.Year

class FrostClimateRepository(
    private val frostV0DataSource: FrostV0DataSource
) : ClimateRepository {

    companion object {
        /** Daily observation elements relevant for avalanche risk assessment. */
        private const val ELEMENT_TEMP = "mean(air_temperature P1D)"
        private const val ELEMENT_PRECIP = "sum(precipitation_amount P1D)"
        private const val ELEMENT_WIND = "mean(wind_speed P1D)"
        private const val ELEMENT_SNOW = "max(surface_snow_thickness P1D)"

        private val ELEMENTS_CSV = listOf(
            ELEMENT_TEMP, ELEMENT_PRECIP, ELEMENT_WIND, ELEMENT_SNOW
        ).joinToString(",")
    }

    override suspend fun getClimateDataForLocation(
        coordinate: Coordinate,
        periodStart: MonthDay,
        periodEnd: MonthDay,
        yearsBack: Int,
    ): LocationClimateData {
        require(yearsBack >= 0)

        val station = getNearestStation(coordinate)

        val referenceTime = buildRepeatingTimeRange(
            periodStart,
            periodEnd,
            yearsBack,
        )

        val obsResponse = frostV0DataSource.getObservations(
            sourceId = station.id,
            elements = ELEMENTS_CSV,
            referenceTime = referenceTime,
        )

        val allObservations = obsResponse.data.flatMap { it.observations }

        if (allObservations.isEmpty())
            throw NoObservationsException(
                stationName = station.name,
                message = "Station ${station.name} has no observations for " +
                        "$periodStart–$periodEnd over the last $yearsBack years",
            )

        val averages = averageByElement(allObservations)

        return LocationClimateData(
            coordinate = coordinate,
            stationName = station.name,
            stationDistanceKm = station.distance,
            stationElevationMasl = station.masl,
            periodStart = periodStart,
            periodEnd = periodEnd,
            yearsBack = yearsBack,
            dataPointCount = allObservations.size,
            avgDailyMeanTemperatureC = averages[ELEMENT_TEMP],
            avgDailyPrecipitationMm = averages[ELEMENT_PRECIP],
            avgDailyMeanWindSpeedMs = averages[ELEMENT_WIND],
            avgDailyMaxSnowDepthCm = averages[ELEMENT_SNOW],
        )
    }

    /** Returns the nearest station to [coordinate], or throws [NoStationFoundException]. */
    private suspend fun getNearestStation(
        coordinate: Coordinate
    ): FrostV0Source
        = frostV0DataSource
        .getNearestSource(coordinate)
        .data
        .firstOrNull()
        ?: throw NoStationFoundException(coordinate)

    /**
     * Incredibly naive and unreliable implementation of slab avalanche
     * risk assessment.
     */
    suspend fun getSlabAvalancheRiskForLocation(
        coordinate: Coordinate
    ) : LocationSlabAvalancheRiskData {

        val currentDate = LocalDate.now()

        val startDate = currentDate.minusDays(21)

        val station = getNearestStation(coordinate)

        val referenceTime = buildRepeatingTimeRange(
            MonthDay.of(startDate.monthValue, startDate.dayOfMonth),
            MonthDay.of(currentDate.monthValue, currentDate.dayOfMonth),
            0
        )

        val observationResponse = frostV0DataSource.getObservations(
            sourceId = station.id,
            elements = ELEMENTS_CSV,
            referenceTime)

        val totalPrecip = observationResponse.data.takeLast(3).sumOf { it.observations.firstOrNull{ it.elementId == ELEMENT_PRECIP }?.value ?: 0.0}

        var newSnowLoad = when (totalPrecip) {
            in 0.0..12.0 -> 0
            in 12.0..25.0 -> 8
            in 25.0..37.0 -> 15
            in 37.0..62.0 -> 20
            else -> 25
        }

        // Assumes at least one existing observation
        val threeDayTempAverage = observationResponse.data.takeLast(3).map { it.observations.firstOrNull{ it.elementId == ELEMENT_TEMP }?.value}.filterNotNull().average()
        val lastTemp = observationResponse.data.filter { it.observations.firstOrNull{ obs -> obs.elementId == ELEMENT_TEMP } != null }.lastOrNull()?.observations?.firstOrNull{ obs -> obs.elementId == ELEMENT_TEMP }?.value



        var warmPrecip = 0.0
        var coldPrecip = 0.0

        var warmPrecipStreak = 0
        var coldPrecipStreak = 0

        for (obs in observationResponse.data.sortedByDescending { it.referenceTime }) {
            val temp = obs.observations.firstOrNull{ it.elementId == ELEMENT_TEMP }?.value
            val precip = obs.observations.firstOrNull{ it.elementId == ELEMENT_PRECIP }?.value

            if (temp == null || precip == null)
                continue

            // Snow-to-Liquid-Ratio
            val getSLR = { t: Double ->
                when {
                    t <= -15.0 -> 20.0
                    t <= -5.0 -> 10.0
                    t <= 0.0 -> 7.0
                    else -> 4.0
                }
            }

            if (temp > -5.0) {
                if (warmPrecip > 150.0 && coldPrecip > 10.0)
                    return LocationSlabAvalancheRiskData(
                        coordinate = coordinate,
                        dangerLevel = "High",
                        dataReliability = 1.0
                    )

                if (warmPrecipStreak == 0)
                    warmPrecip = precip * getSLR(temp)
                else
                    warmPrecip += precip * getSLR(temp)

                warmPrecipStreak++
                coldPrecipStreak = 0
            }
            else {
                if (coldPrecipStreak == 0)
                    coldPrecip = precip * getSLR(temp)
                else
                    coldPrecip += precip * getSLR(temp)

                coldPrecipStreak++
                warmPrecipStreak = 0
            }
        }


        var weakLayerDepth = 0.0
        var slabLayerDepth = 0.0


        var coldPrecip1 = 0.0
        for (obs in observationResponse.data.sortedBy { it.referenceTime }) {
            val temp = obs.observations.firstOrNull { it.elementId == ELEMENT_TEMP }?.value   ?: continue
            val precip = obs.observations.firstOrNull { it.elementId == ELEMENT_PRECIP }?.value ?: 0.0

            if (temp <= 0.0) {
                coldPrecip1 += precip
            }
            else if (precip > 2.0) {
                weakLayerDepth = coldPrecip1
                coldPrecip1 = 0.0
            }
        }

        val weakLayerPresence = when (weakLayerDepth) {
            in 0.0 .. 12.0 -> 0
            in 12.0 .. 25.0 -> 10
            in 25.0 .. 50.0 -> 20
            else -> 30
        }

        if (warmPrecip > 150.0 && coldPrecip > 10.0)
            return LocationSlabAvalancheRiskData(
                coordinate = coordinate,
                dangerLevel = "High",
                dataReliability = 1.0
            )

        return LocationSlabAvalancheRiskData(
            coordinate = coordinate,
            dangerLevel = "Low",
            dataReliability = 1.0
        )
    }

}
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
): LocalDate
    = LocalDate.of(
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
