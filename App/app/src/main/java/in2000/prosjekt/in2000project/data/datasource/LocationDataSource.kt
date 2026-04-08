package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.model.LocationResponse
import in2000.prosjekt.in2000project.data.model.Name
import in2000.prosjekt.in2000project.network.KtorClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class LocationDataSource {
    suspend fun getNamesResponse(query: String) : List<Name> {
        return try {
            val httpResponse: LocationResponse = KtorClient.client.get(query).body()
            httpResponse.navn
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}