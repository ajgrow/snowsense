package in2000.prosjekt.in2000project.ui.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.NamedLocation
import in2000.prosjekt.in2000project.data.repository.LocationRepository
import in2000.prosjekt.in2000project.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState (
    val searchTextFieldState: TextFieldState = TextFieldState(),
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

    //initialize with mock data for the MVP:
    init{
        // Update search results as user types
        viewModelScope.launch {
            snapshotFlow { _searchState.value.searchTextFieldState.text.toString() }
                .debounce(300) //wait 300ms after user stops typing
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

    fun updateSearchTextFieldState(textFieldState: TextFieldState){
        //make a copy of the current state and change the searchQuery to the new query
        _searchState.update { it.copy(searchTextFieldState = textFieldState) }
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