package in2000.prosjekt.in2000project.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object GeoJsonRepository {
    suspend fun fetchGeoJson(): String = withContext(Dispatchers.IO) {
        URL(
            "https://kart.nve.no/enterprise/rest/services/SnoskredAktsomhet/MapServer/1/query?where=1%3D1&outFields=objekttype,skredtype&outSR=4326&f=geojson"
        ).readText()
    }
}