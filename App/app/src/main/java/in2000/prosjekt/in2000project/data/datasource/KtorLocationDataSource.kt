package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.datasource.dto.LocationDto
import in2000.prosjekt.in2000project.data.datasource.dto.LocationResponseDto
import in2000.prosjekt.in2000project.data.datasource.LocationDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.net.URLEncoder

private const val BASE_URL = "https://api.kartverket.no/stedsnavn/v1/navn"
private const val RESULTS_PER_PAGE = 10
private const val FILTER = "navn.representasjonspunkt,navn.skrivemåte,navn.navneobjekttype"

class KtorLocationDataSource(
    private val client: HttpClient
) : LocationDataSource {
    override suspend fun getNamesResponse(query: String) : List<LocationDto>? {
        val fullQuery = buildLocationQuery(query)
        return try {
            val httpResponse: LocationResponseDto = client.get(fullQuery).body()
            httpResponse.navn
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun buildLocationQuery(searchQuery: String): String {
        val encodedQuery = URLEncoder.encode(searchQuery, "UTF-8")
        val encodedFilter = URLEncoder.encode(FILTER, "UTF-8")
        return "$BASE_URL?sok=$encodedQuery" +
                "&fuzzy=true" +
                "&utkoordsys=4258" +
                "&treffPerSide=$RESULTS_PER_PAGE" +
                "&side=1" +
                "&filtrer=$encodedFilter"
    }
}


