package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.NveDataSource
import in2000.prosjekt.in2000project.data.model.GeoJson

class NveRepository (
    private val nveDataSource: NveDataSource
) {
    suspend fun getGeoJson(lat: Double, lon: Double, buffer: Int): GeoJson {
        return nveDataSource.getGeoJson(lat, lon, buffer)
    }
}