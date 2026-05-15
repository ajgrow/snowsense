package in2000.prosjekt.in2000project.domain.repository

import in2000.prosjekt.in2000project.domain.model.NamedLocation

/**
 * Repository for storing and retrieving user search data.
 */
interface SearchRepository {

    /**
     * Persists the given list of favorite locations.
     *
     * @param list The favorites to save.
     */
    fun saveFavorites(list: List<NamedLocation>)

    /**
     * Returns the saved list of favorite locations.
     *
     * @return The saved favorites, or an empty list if none exist.
     */
    fun loadFavorites(): List<NamedLocation>

    /**
     * Persists the given list of recent searches.
     *
     * @param list The recent searches to save.
     */
    fun saveRecentSearches(list: List<NamedLocation>)

    /**
     * Returns the saved list of recent searches.
     *
     * @return The saved recent searches, or an empty list if none exist.
     */
    fun loadRecentSearches(): List<NamedLocation>
}