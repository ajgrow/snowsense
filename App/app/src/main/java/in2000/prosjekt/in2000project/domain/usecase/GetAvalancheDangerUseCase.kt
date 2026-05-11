package in2000.prosjekt.in2000project.domain.usecase

import in2000.prosjekt.in2000project.domain.SlabAvalancheRiskAssessor
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.enums.DangerLevel
import in2000.prosjekt.in2000project.domain.repository.AvalancheRiskRepository
import in2000.prosjekt.in2000project.domain.repository.WeatherRepository

class GetAvalancheDangerUseCase(
    private val weatherRepository: WeatherRepository,
    private val avalancheRiskRepository: AvalancheRiskRepository,
    private val slabAvalancheRiskAssessor: SlabAvalancheRiskAssessor
) {
    suspend operator fun invoke(
        coordinate: Coordinate
    ): DangerLevel {
        val isDangerZone = avalancheRiskRepository
            .getGeoJsonStatus(
                coordinate.latitude,
                coordinate.longitude,
                10)

        if (!isDangerZone)
            return DangerLevel.LOW

        val records = try {
            weatherRepository.getWeatherRecordsForLocation(
                coordinate,
                lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
            )
        } catch(e: Exception) {
            emptyList()
        }

        // -1 days to account for current date data missing
        if (records.isEmpty()
            || records.size < (SlabAvalancheRiskAssessor.LOOKBACK_DAYS - 1))
            return DangerLevel.UNKNOWN

        return slabAvalancheRiskAssessor.assess(records)
    }
}