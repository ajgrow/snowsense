package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.NveDataSource

class NveRepository (
    private val nveDataSource: NveDataSource
) {
    suspend fun getGeoJson(): String{
        return nveDataSource.fetchGeoJson()
    }
}