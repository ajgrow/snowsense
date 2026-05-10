package in2000.prosjekt.in2000project.domain.repository

import in2000.prosjekt.in2000project.domain.model.NamedLocation

/**
 * Repository for location search data from Kartverket.
 */
interface LocationRepository {

    /**
     * Returns a list of named locations matching the given search query.
     *
     * @param query The search string to look up place names for.
     * @return A list of [NamedLocation] matching the query, or an empty list if none were found.
     */
    suspend fun getNamedLocations(query: String): List<NamedLocation>
}