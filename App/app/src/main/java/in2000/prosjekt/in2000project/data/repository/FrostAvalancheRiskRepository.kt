package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationsAtRefTime
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0Source
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.DailyWeatherRecord
import in2000.prosjekt.in2000project.data.model.LocationSlabAvalancheRiskData
import in2000.prosjekt.in2000project.data.model.NoObservationsException
import in2000.prosjekt.in2000project.data.model.NoStationFoundException
import in2000.prosjekt.in2000project.data.model.enums.ElementType
import java.time.LocalDate

class FrostAvalancheRiskRepository(
    private val frostV0DataSource: FrostV0DataSource,
): AvalancheRiskRepository {

    override suspend fun getSlabAvalancheRiskForLocation(
        coordinate: Coordinate,
        currentDate: LocalDate,
    ): LocationSlabAvalancheRiskData {
        val dataSource = frostV0DataSource.getNearestSource(coordinate)
            ?: throw NoStationFoundException(coordinate)

        val startDate = currentDate.minusDays(
            SlabAvalancheRiskAssessor
                .LOOKBACK_DAYS.toLong())

        val repeatingTimeRange = FrostReferenceTime.of(
            startDate = startDate,
            endDate = currentDate,
        )

        val records = dataSource.getDailyWeatherRecords(repeatingTimeRange)

        val dangerLevel = SlabAvalancheRiskAssessor.assess(records)

        return LocationSlabAvalancheRiskData(
            coordinate = coordinate,
            dangerLevel = dangerLevel,
        )
    }

    private suspend fun FrostV0Source.getDailyWeatherRecords(
        repeatingTimeRange: FrostReferenceTime
    ): List<DailyWeatherRecord> {
        val requiredElements = listOf(
            ElementType.TEMPERATURE_MEAN_P1D.frostElementId,
            ElementType.PRECIPITATION_SUM_P1D.frostElementId
        )

        val elementsCsv = requiredElements.joinToString(",")

        val observationResponse = frostV0DataSource.getObservations(
            this.id ?: "",
            elementsCsv,
            repeatingTimeRange.value)

        if (observationResponse == null) {
            return emptyList()
        }

        val observations = observationResponse.data

        if (observations.isEmpty()) return emptyList()

        val hasAllElements = observations.all { atRefTime ->
            requiredElements.all { elementId ->
                atRefTime.observations.any { it.elementId == elementId }
            }
        }

        if (!hasAllElements) return emptyList()

        return observations.map { it.toDailyWeatherRecord() }
    }

    private fun FrostV0ObservationsAtRefTime.toDailyWeatherRecord(
    ) = DailyWeatherRecord(
        LocalDate
            .parse(referenceTime.substringBefore("T")),
        observations.firstOrNull {
            it.elementId == ElementType.TEMPERATURE_MEAN_P1D.frostElementId }
            ?.value,
        observations.firstOrNull {
            it.elementId == ElementType.PRECIPITATION_SUM_P1D.frostElementId }
            ?.value,
    )
}
