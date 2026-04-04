package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.model.GeoJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import java.net.URL
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import kotlin.collections.emptyList

class NveDataSource {

    private val httpClient = HttpClient(Android){
        install(ContentNegotiation){
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getGeoJson(): GeoJson {
        return try {
            httpClient
                .get("https://kart.nve.no/enterprise/rest/services/SnoskredAktsomhet/MapServer/1/query?where=1%3D1&outFields=objekttype,skredtype&returnGeometry=true&outSR=4326&f=geojson")
                .body<GeoJson>()
        } catch (e: Exception){
            e.printStackTrace()
            println("Feil: ${e.message}")
            GeoJson(
                exceededTransferLimit = false,
                features = emptyList(),
                properties = null,
                type = "FeatureCollection")
        }
    }
}
