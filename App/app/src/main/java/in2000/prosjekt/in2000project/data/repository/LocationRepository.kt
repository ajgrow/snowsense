package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.LocationDataSource
import in2000.prosjekt.in2000project.data.model.Location

class LocationRepository {

    private val dataSource = LocationDataSource()

    suspend fun getNames(query:String) : List<Location> {
        return dataSource.getNamesResponse(query)
    }
}