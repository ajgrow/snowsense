package in2000.prosjekt.in2000project.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.NamedLocation
import in2000.prosjekt.in2000project.domain.repository.LocationRepository
import in2000.prosjekt.in2000project.domain.repository.SearchRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the state of the search screen.
 *
 * @property query The current search query.
 * @property searchResults The list of search results.
 * @property favorites The list of favorite locations.
 * @property recentSearches The list of recent searches.
 * @property awaitingFavoriteName The coordinate of the location being favorited.
 * @property isSearchActive Whether the search is active.
 */
data class SearchUiState (
    val query: String = "",
    val searchResults: List<NamedLocation> = emptyList(),
    val favorites: List<NamedLocation> = emptyList(),
    val recentSearches: List<NamedLocation> = emptyList(),
    val awaitingFavoriteName: Coordinate? = null,
    val isSearchActive: Boolean = false
)
@OptIn(FlowPreview::class)

/**
 * Shared ViewModel for search.
 *
 * @param searchRepository The repository for searching locations.
 * @param locationRepository The repository for getting location information.
 */
class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val locationRepository: LocationRepository
): ViewModel(){
    private val _searchState = MutableStateFlow(SearchUiState(
        favorites = searchRepository.loadFavorites(),
        recentSearches = searchRepository.loadRecentSearches()
    ))
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    //initialize with mock data for the MVP:
    init{
        // Update search results as user types
        viewModelScope.launch {
            _searchState
                .map {
                    it.query
                }
                .distinctUntilChanged()
                .debounce(300)
                .collect { query ->
                    if (query.isNotBlank()) {
                        val results = locationRepository.getNamedLocations(query)
                        _searchState.update { it.copy(searchResults = results) }
                    } else {
                        _searchState.update { it.copy(searchResults = emptyList()) }
                    }
                }
        }
    }

    /**
     * Updates the search query.
     *
     * @param query The new search query.
     * @param limit The maximum number of results to return.
     * @param offset The offset for pagination.
     */
    fun onQueryChange(query: String){
        _searchState.update { it.copy(query = query.take(50)) }
    }

    /**
     * Clears the search query.
     */
    fun clearQuery() {
        _searchState.update { it.copy(query = "", searchResults = emptyList()) }
    }

    /**
     * Selects a search result.
     *
     * @param location The location to select.
     */
    fun selectSearchResult(location: NamedLocation) {
        _searchState.update {
            it.copy(
                query = ""
            )
        }
        addRecentSearch(location)
    }

    /**
     * Adds a recent search.
     *
     * @param location The location to add as a recent search.
     */
    fun addRecentSearch(location: NamedLocation) {
        _searchState.update {
            val updated = (listOf(location) + it.recentSearches)
                .distinct()
                .take(5)
            it.copy(recentSearches = updated)
        }
        searchRepository.saveRecentSearches(_searchState.value.recentSearches)
    }

    /**
     * Removes a recent search.
     *
     * @param location The location to remove from the recent searches.
     */
    fun removeRecentSearch(location: NamedLocation) {
        _searchState.update {
            it.copy(recentSearches = it.recentSearches - location)
        }
        searchRepository.saveRecentSearches(_searchState.value.recentSearches)
    }

    /**
     * Adds a pending favorite.
     *
     * @param coordinate The coordinate of the location to add as a pending favorite.
     */
    fun addPendingFavorite(coordinate: Coordinate) {
        _searchState.update {
            it.copy(awaitingFavoriteName = coordinate)
        }
    }

    /**
     * Cancels the pending favorite.
     */
    fun cancelPendingFavorite() {
        _searchState.update {
            it.copy(awaitingFavoriteName = null)
        }
    }

    /**
     * Names a favorite location.
     *
     * @param name The name to give the favorite location.
     */
    fun nameFavorite(name: String) {
        _searchState.value.awaitingFavoriteName?.let { favoriteCoordinate ->
            addFavoriteLocation(
                NamedLocation(
                    name,
                    favoriteCoordinate
                )
            )
            cancelPendingFavorite()
        }
    }

    /**
     * Adds a favorite location.
     *
     * @param location The location to add as a favorite.
     */
    private fun addFavoriteLocation(location: NamedLocation) {
        _searchState.update {
            val updated = (listOf(location) + it.favorites)
                .distinct()
                .take(5)
            it.copy(favorites = updated)
        }
        searchRepository.saveFavorites(_searchState.value.favorites)
    }

    /**
     * Removes a favorite location.
     *
     * @param location The location to remove from the favorites.
     */
    fun removeFavoriteLocation(location: NamedLocation) {
        _searchState.update {
            it.copy(favorites = it.favorites - location)
        }
        searchRepository.saveFavorites(_searchState.value.favorites)
    }

    /**
     * Sets the search active state.
     *
     * @param active The new active state.
     */
    fun setSearchActive(active: Boolean) {
        _searchState.update { it.copy(isSearchActive = active) }
    }
}