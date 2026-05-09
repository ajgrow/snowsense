package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationsAtRefTimeDto
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0SourceDto
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.DailyWeatherRecord
import in2000.prosjekt.in2000project.domain.model.LocationSlabAvalancheRiskData
import in2000.prosjekt.in2000project.data.model.exceptions.NoObservationsException
import in2000.prosjekt.in2000project.data.model.exceptions.NoStationFoundException
import in2000.prosjekt.in2000project.domain.model.enums.ElementType
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

    private suspend fun FrostV0SourceDto.getDailyWeatherRecords(
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
            throw NoObservationsException(
                this.name, elementsCsv, repeatingTimeRange.value
            )
        }

        val observations = observationResponse.data


        // filter out today's observations which are incomplete,
        // since we want to base the risk on complete data

        val today = LocalDate.now()
        val completeObservations = observations
            .filter { atRefTime ->
                LocalDate.parse(
                    atRefTime.referenceTime.substringBefore("T")
                ) < today
            }

        val records = completeObservations
            .filter { atRefTime ->
                requiredElements.all { elementId ->
                    atRefTime.observations.any { it.elementId == elementId }
                }
            }
            .map { it.toDailyWeatherRecord() }

        if (records.isEmpty())
            throw NoObservationsException(
                this.name, elementsCsv, repeatingTimeRange.value
            )

        return records
    }

    private fun FrostV0ObservationsAtRefTimeDto.toDailyWeatherRecord(
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
