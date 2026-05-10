package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.NveDataSource

class NveRepository (
    private val nveDataSource: NveDataSource
) {
    suspend fun getGeoJsonStatus(lat: Double, lon: Double, buffer: Int): Boolean {
        return nveDataSource.getGeoJson(lat, lon, buffer).features?.isNotEmpty() ?: false
    }
}