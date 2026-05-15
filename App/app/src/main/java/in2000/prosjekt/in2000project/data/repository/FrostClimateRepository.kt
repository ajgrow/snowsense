package in2000.prosjekt.in2000project.data.repository


import in2000.prosjekt.in2000project.data.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationDto
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationResponseDto
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.LocationClimateData
import in2000.prosjekt.in2000project.domain.model.LocationClimateDataPoint
import in2000.prosjekt.in2000project.domain.model.enums.ElementType
import in2000.prosjekt.in2000project.domain.model.exceptions.NoObservationsException
import in2000.prosjekt.in2000project.domain.model.exceptions.NoStationFoundException
import in2000.prosjekt.in2000project.domain.repository.ClimateRepository
import java.time.MonthDay

class FrostClimateRepository(
    private val frostV0DataSource: FrostV0DataSource,
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

        val referenceTime = FrostReferenceTime.fromMonthDayRange(
            periodStart,
            periodEnd,
            yearsBack,
        ).value

        val elementsCsv = ElementType.entries
            .joinToString(",") {
                it.frostElementId
            }

        val obsResponse = frostV0DataSource.getObservations(
            station.id ?: "",
            elementsCsv,
            referenceTime,
        )

        if (obsResponse == null) {
            throw NoObservationsException(
                station.name ?: "",
                elementsCsv,
                referenceTime
            )
        }

        val observationCount = obsResponse.data
            .fold(0) { acc, obs ->
                acc + obs.observations.size
            }

        if (observationCount == 0)
            throw NoObservationsException(
                station.name ?: "",
                elementsCsv,
                referenceTime
            )

        val yearlyAverages = obsResponse.getYearlyDataPoints()

        return LocationClimateData(
            coordinate = coordinate,
            stationName = station.name,
            stationDistanceKm = station.distance,
            stationElevationMasl = station.masl,
            periodStart = periodStart,
            periodEnd = periodEnd,
            yearsBack = yearsBack,
            dataPointCount = observationCount,
            yearlyAverages = yearlyAverages
        )
    }
}

private fun FrostV0ObservationResponseDto.getYearlyDataPoints(
): List<LocationClimateDataPoint> = this
    .groupObservationsByYear()
    .mapValues { it.value.averageByElement() }
    .map { (year, elementAverages) ->
        createDataPointForYear(year, elementAverages)
    }
    .flatten()

private fun FrostV0ObservationResponseDto.groupObservationsByYear(
): Map<Int, List<FrostV0ObservationDto>> = this
    .data
    .groupBy(
        { it.referenceTime.take(4).toIntOrNull() ?: 0 },
        { it.observations })
    .filter { it.key != 0 }
    .mapValues { it.value.flatten() }

/** Mean observation value per Frost elementId. */
internal fun List<FrostV0ObservationDto>.averageByElement(
): Map<String, Double> = this.groupBy { it.elementId }
    .mapValues { groupedObservations ->
        groupedObservations.value
            .map { observation -> observation.value }
            .average()
    }

// Pair years and ElementType averages, skipping missing elements.
private fun createDataPointForYear(
    year: Int,
    elementAverages: Map<String, Double>,
): List<LocationClimateDataPoint> = ElementType.entries.mapNotNull { type ->
    elementAverages[type.frostElementId]?.let { avgValue ->
        LocationClimateDataPoint(year, type, type.defaultUnit, avgValue)
    }
}