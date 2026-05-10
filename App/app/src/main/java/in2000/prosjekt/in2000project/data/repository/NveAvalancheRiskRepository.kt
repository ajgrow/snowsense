package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.AvalancheRiskDataSource
import in2000.prosjekt.in2000project.domain.repository.AvalancheRiskRepository

class NveAvalancheRiskRepository (
    private val dataSource: AvalancheRiskDataSource
) : AvalancheRiskRepository {
    override suspend fun getGeoJsonStatus(lat: Double, lon: Double, buffer: Int): Boolean {
        return dataSource.getGeoJson(lat, lon, buffer).features?.isNotEmpty() ?: false
    }
}