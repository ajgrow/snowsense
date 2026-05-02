package in2000.prosjekt.in2000project.ui.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.datasource.LocationQueryBuilder
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.NamedLocation
import in2000.prosjekt.in2000project.data.repository.LocationRepository
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
    val awaitingFavoriteName: Coordinate? = null
)
class SearchViewModel: ViewModel(){

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()
    val queryBuilder = LocationQueryBuilder()
    val locationRepository = LocationRepository()


    //initialize with mock data for the MVP:
    init{
        // Update search results as user types
        viewModelScope.launch {
            snapshotFlow { _searchState.value.searchTextFieldState.text.toString() }
                .debounce(300) //wait 300ms after user stops typing
                .collect { query ->
                    if (query.isNotBlank()) {
                        val built = queryBuilder.buildQuery(query)
                        val results = locationRepository.getNamedLocations(built)
                        _searchState.update { it.copy(searchResults = results) }
                    } else {
                        _searchState.update { it.copy(searchResults = emptyList()) }
                    }
                }
        }

        viewModelScope.launch {
            //Once we have a repository in place, we will call it here
            //example: val data = searchRepository.getData()
            _searchState.update { currentState ->
                //make a new copy of currentState (SearchUiState) with these changes, and update _searchState accordingly (.update)
                currentState.copy(
                    favorites = emptyList(),
                    recentSearches = emptyList()
                )
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
    }

    fun removeRecentSearch(location: NamedLocation) {
        _searchState.update {
            it.copy(recentSearches = it.recentSearches - location)
        }
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
    }

    fun removeFavoriteLocation(location: NamedLocation) {
        _searchState.update {
            it.copy(favorites = it.favorites - location)
        }
    }

    fun onSearch(){
        val query = queryBuilder.buildQuery(_searchState.value.searchTextFieldState.text.toString())
        viewModelScope.launch {
            val results = locationRepository.getNamedLocations(query)
            _searchState.update { it.copy(searchResults = results) }
        }
    }
}