package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationsAtRefTimeDto
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0SourceDto
import in2000.prosjekt.in2000project.domain.model.exceptions.NoObservationsException
import in2000.prosjekt.in2000project.domain.model.exceptions.NoStationFoundException
import in2000.prosjekt.in2000project.data.datasource.FrostV0DataSource
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.DailyWeatherRecord
import in2000.prosjekt.in2000project.domain.model.enums.ElementType
import in2000.prosjekt.in2000project.domain.repository.WeatherRepository
import java.time.LocalDate

class FrostWeatherRepository(
    private val frostV0DataSource: FrostV0DataSource,
): WeatherRepository {

    override suspend fun getWeatherRecordsForLocation(
        coordinate: Coordinate,
        currentDate: LocalDate,
        lookBackDays: Int
    ): List<DailyWeatherRecord> {
        val dataSource = frostV0DataSource.getNearestSource(coordinate)
            ?: throw NoStationFoundException(coordinate)

        val startDate = currentDate.minusDays(lookBackDays.toLong())

        val repeatingTimeRange = FrostReferenceTime.of(
            startDate = startDate,
            endDate = currentDate,
        )

        val records = getDailyWeatherRecords(
            dataSource,
            repeatingTimeRange,
            currentDate)

        return records
    }

    private suspend fun getDailyWeatherRecords(
        source: FrostV0SourceDto,
        repeatingTimeRange: FrostReferenceTime,
        currentDate: LocalDate,
    ) : List<DailyWeatherRecord> {
        val requiredElements = listOf(
            ElementType.TEMPERATURE_MEAN_P1D.frostElementId,
            ElementType.PRECIPITATION_SUM_P1D.frostElementId
        )

        val elementsCsv = requiredElements.joinToString(",")

        val observationResponse = frostV0DataSource.getObservations(
            source.id ?: "",
            elementsCsv,
            repeatingTimeRange.value)

        if (observationResponse == null) {
            throw NoObservationsException(
                source.name, elementsCsv, repeatingTimeRange.value
            )
        }

        val observations = observationResponse.data


        // filter out current date's observations which are incomplete,
        // since we want to base the risk on complete data

        val completeObservations = observations
            .filter { atRefTime ->
                LocalDate.parse(
                    atRefTime.referenceTime.substringBefore("T")
                ) < currentDate
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
                source.name, elementsCsv, repeatingTimeRange.value
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
