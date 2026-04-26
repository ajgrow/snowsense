package in2000.prosjekt.in2000project.data.repository


import in2000.prosjekt.in2000project.data.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0Observation
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0Source
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.DangerLevel
import in2000.prosjekt.in2000project.data.model.LocationClimateData
import in2000.prosjekt.in2000project.data.model.LocationSlabAvalancheRiskData
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
        val referenceTime = createReferenceTimeString(
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

        if (allObservations.isEmpty())
            throw NoObservationsException(
                station.name,
                elementsCsv,
                referenceTime)

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

    /**
     * Incredibly naive and unreliable implementation of slab avalanche
     * risk assessment.
     */
    suspend fun getSlabAvalancheRiskForLocation(
        coordinate: Coordinate,
        currentDate: LocalDate = LocalDate.now()
    ) : LocationSlabAvalancheRiskData {

        //define time range
        val startDate = currentDate.minusDays(21)

        //find nearest station
        val station = getNearestStation(coordinate)

        //build time query
        val referenceTime = createReferenceTimeString(
            MonthDay.of(startDate.monthValue, startDate.dayOfMonth),
            MonthDay.of(currentDate.monthValue, currentDate.dayOfMonth),
            0
        )

        //fetch temperature and precipitation
        val observationResponse = frostV0DataSource.getObservations(
            station.id,
            elementsCsv,
            referenceTime)

        if (observationResponse.data.flatMap { it.observations }.isEmpty())
            throw NoObservationsException(
                station.name,
                elementsCsv,
                referenceTime
            )

        //recent snowfall estimate from last 3 days
        val totalPrecip = observationResponse.data.take(3).sumOf { it.observations.firstOrNull{ it.elementId == ElementType.PRECIPITATION_SUM_P1D.frostElementId }?.value ?: 0.0}

        //Score for newSnowLoad
        var newSnowLoad = when (totalPrecip) {
            in 0.0..12.0 -> 0
            in 12.0..25.0 -> 8
            in 25.0..37.0 -> 15
            in 37.0..62.0 -> 20
            else -> 25
        }

        // Temperature metrics - Assumes at least one existing observation
        val threeDayTempAverage = observationResponse.data.take(3).map { it.observations.firstOrNull{ it.elementId == ElementType.TEMPERATURE_MEAN_P1D.frostElementId }?.value}.filterNotNull().average()
        val lastTemp = observationResponse.data.filter { it.observations.firstOrNull{ obs -> obs.elementId == ElementType.TEMPERATURE_MEAN_P1D.frostElementId } != null }.firstOrNull()?.observations?.firstOrNull{ obs -> obs.elementId == ElementType.TEMPERATURE_MEAN_P1D.frostElementId }?.value

        var tempChange = if (lastTemp != null) lastTemp - threeDayTempAverage else 0.0

        val tempTrend = when {
            tempChange < 1.0 -> 0
            tempChange in 1.0..3.0 -> 8
            tempChange in 3.0..6.0 -> 14
            else -> 20
        }

        var weakLayerDepth = 0.0
        var slabLayerDepth = 0.0


        var weakLayerBase = false

        var coldPrecip1 = 0.0
        for (obs in observationResponse.data.sortedBy { it.referenceTime }) {
            val temp = obs.observations.firstOrNull { it.elementId == ElementType.TEMPERATURE_MEAN_P1D.frostElementId }?.value   ?: continue
            val precip = obs.observations.firstOrNull { it.elementId == ElementType.PRECIPITATION_SUM_P1D.frostElementId }?.value ?: 0.0

            if (temp <= 0.0) {
                coldPrecip1 += precip
            }
            else if (precip > 2.0) {
                weakLayerDepth = coldPrecip1
                coldPrecip1 = 0.0
            }

            if (temp > -5.0 && weakLayerBase){
                slabLayerDepth += precip
            }
            else if (temp <= -5.0){
                weakLayerBase = true
            }
        }

        val slabFormation = when{
            slabLayerDepth <= 17.0 -> 0
            slabLayerDepth <= 50.0 -> 10
            slabLayerDepth <= 100.0 -> 18
            else -> 25
        }

        val weakLayerPresence = when (weakLayerDepth) {
            in 0.0 .. 12.0 -> 0
            in 12.0 .. 25.0 -> 10
            in 25.0 .. 50.0 -> 20
            else -> 30
        }

        val totalScore = newSnowLoad + weakLayerPresence + slabFormation + tempTrend

        val dangerLevel = when (totalScore) {
            in 0..15 -> DangerLevel.LOW
            in 16..35 -> DangerLevel.MODERATE
            in 36..55 -> DangerLevel.CONSIDERABLE
            in 56..75 -> DangerLevel.HIGH
            else -> DangerLevel.VERY_HIGH
        }

        return LocationSlabAvalancheRiskData(
            coordinate = coordinate,
            dangerLevel = dangerLevel,
            dataReliability = 1.0
        )
    }

}

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
     * Creates a Frost v0 ISO-8601 referenceTime string.
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
    internal fun createReferenceTimeString(
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

        if (yearsBack == 0)
            return "$firstStart/$firstEnd"

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
