package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.NveDataSource
import in2000.prosjekt.in2000project.data.model.GeoJson

class NveRepository (
    private val nveDataSource: NveDataSource = NveDataSource()
) {
    suspend fun getGeoJson(): GeoJson {
        return nveDataSource.getGeoJson()
    }
}