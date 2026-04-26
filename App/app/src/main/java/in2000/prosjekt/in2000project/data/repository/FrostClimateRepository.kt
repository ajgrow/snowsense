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

        val station = frostV0DataSource.getNearestSource(coordinate)
            ?: throw NoStationFoundException(coordinate)

        // ISO-8601 repeating time range (R10/2014-03-01/2014-04-01/P1Y)
        val referenceTime = FrostReferenceTime.fromMonthDayRange(
            periodStart,
            periodEnd,
            yearsBack,
        ).value

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
        val station = frostV0DataSource.getNearestSource(coordinate)
            ?: throw NoStationFoundException(coordinate)

        //build time query
        val referenceTime = FrostReferenceTime.fromMonthDayRange(
            MonthDay.of(startDate.monthValue, startDate.dayOfMonth),
            MonthDay.of(currentDate.monthValue, currentDate.dayOfMonth),
            0
        ).value

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

private val elementsCsv: String =
    ElementType.entries.joinToString(",") { it.frostElementId }

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
