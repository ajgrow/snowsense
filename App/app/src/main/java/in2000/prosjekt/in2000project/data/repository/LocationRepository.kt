package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.LocationDataSource
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.NamedLocation
import in2000.prosjekt.in2000project.util.LocationTypeTranslations

class LocationRepository {

    private val dataSource = LocationDataSource()

    suspend fun getNamedLocations(query:String) : List<NamedLocation> {
        return dataSource.getNamesResponse(query)?.map {
            NamedLocation(
                name = it.skrivemåte,
                coordinate = Coordinate(
                    it.representasjonspunkt.nord,
                    it.representasjonspunkt.øst
                ),
                locationTypeNo = it.navneobjekttype,
                locationTypeEn = LocationTypeTranslations.translations[it.navneobjekttype] ?: it.navneobjekttype
            )
        } ?: emptyList()
    }
}