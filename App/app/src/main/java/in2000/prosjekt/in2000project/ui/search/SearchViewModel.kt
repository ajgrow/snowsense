package in2000.prosjekt.in2000project.ui.search

import android.app.Application
import android.content.Context
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.datasource.buildLocationQuery
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.NamedLocation
import in2000.prosjekt.in2000project.data.repository.LocationRepository
import in2000.prosjekt.in2000project.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class SearchUiState (
    val query: String = "",
    val searchResults: List<NamedLocation> = emptyList(),
    val favorites: List<NamedLocation> = emptyList(),
    val recentSearches: List<NamedLocation> = emptyList(),
    val awaitingFavoriteName: Coordinate? = null,
    val isSearchActive: Boolean = false
)
class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val locationRepository: LocationRepository
): ViewModel(){
    private val _searchState = MutableStateFlow(SearchUiState(
        favorites = searchRepository.loadFavorites(),
        recentSearches = searchRepository.loadRecentSearches()
    ))
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    init {
        viewModelScope.launch {
            _searchState
                .map {
                    it.query
                }
                .distinctUntilChanged()
                .debounce(300)
                .collect { query ->
                    if (query.isNotBlank()) {
                        val built = buildLocationQuery(query)
                        val results = locationRepository.getNamedLocations(built)
                        _searchState.update { it.copy(searchResults = results) }
                    } else {
                        _searchState.update { it.copy(searchResults = emptyList()) }
                    }
                }
        }
    }

    fun onQueryChange(query: String){
        _searchState.update { it.copy(query = query) }
    }

    fun addRecentSearch(location: NamedLocation) {
        _searchState.update {
            val updated = (listOf(location) + it.recentSearches)
                .distinct()
                .take(5)
            it.copy(recentSearches = updated)
        }
        searchRepository.saveRecentSearches(_searchState.value.recentSearches)
    }

    fun removeRecentSearch(location: NamedLocation) {
        _searchState.update {
            it.copy(recentSearches = it.recentSearches - location)
        }
        searchRepository.saveRecentSearches(_searchState.value.recentSearches)
    }

    fun addPendingFavorite(coordinate: Coordinate) {
        _searchState.update {
            it.copy(awaitingFavoriteName = coordinate)
        }
    }

    fun cancelPendingFavorite() {
        _searchState.update {
            it.copy(awaitingFavoriteName = null)
        }
    }

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

    private fun addFavoriteLocation(location: NamedLocation) {
        _searchState.update {
            val updated = (listOf(location) + it.favorites)
                .distinct()
                .take(5)
            it.copy(favorites = updated)
        }
        searchRepository.saveFavorites(_searchState.value.favorites)
    }

    fun removeFavoriteLocation(location: NamedLocation) {
        _searchState.update {
            it.copy(favorites = it.favorites - location)
        }
        searchRepository.saveFavorites(_searchState.value.favorites)
    }

    fun setSearchActive(active: Boolean) {
        _searchState.update { it.copy(isSearchActive = active) }
    }
}