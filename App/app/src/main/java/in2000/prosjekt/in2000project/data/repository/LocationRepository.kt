package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.LocationDataSource
import in2000.prosjekt.in2000project.data.model.Name

class LocationRepository {

    private val dataSource = LocationDataSource()

    suspend fun getNames(query:String) : List<Name> {
        return dataSource.getNamesResponse(query)
    }
}