package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.datasource.dto.LocationDto

/**
 * Data source for location search data from Kartverket Stedsnavn.
 */
interface LocationDataSource {

    /**
     * Fetches a list of locations matching the given search query.
     *
     * @param query The search string to look up place names for.
     * @return A list of [in2000.prosjekt.in2000project.data.datasource.dto.LocationDto] matching the query, or an empty list if the request fails.
     */
    suspend fun getNamesResponse(query: String): List<LocationDto>?
}