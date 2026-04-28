package in2000.prosjekt.in2000project.ui.home

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.data.datasource.LocationQueryBuilder
import in2000.prosjekt.in2000project.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.NamedLocation
import kotlinx.coroutines.flow.debounce

data class HomeUiState(
    val searchTextFieldState: TextFieldState = TextFieldState(),
    val searchResults: List<NamedLocation> = emptyList(),
    val favorites: List<NamedLocation> = emptyList(),
    val recentSearches: List<NamedLocation> = emptyList()
)

class HomeViewModel: ViewModel(){

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()
    val queryBuilder = LocationQueryBuilder()
    val locationRepository = LocationRepository()

    //initialize with mock data for the MVP:
    init{
        // Update search results as user types
        viewModelScope.launch {
            snapshotFlow { _homeState.value.searchTextFieldState.text.toString() }
                .debounce(300) //wait 300ms after user stops typing
                .collect { query ->
                    if (query.isNotBlank()) {
                        val built = queryBuilder.buildQuery(query)
                        val results = locationRepository.getNames(built)
                        _homeState.update { it.copy(searchResults = results) }
                    } else {
                        _homeState.update { it.copy(searchResults = emptyList()) }
                    }
                }
        }

        viewModelScope.launch {
            //Once we have a repository in place, we will call it here
            //example: val data = HomeRepository.getData()
            _homeState.update { currentState ->
                //make a new copy of currentState (HomeUiState) with these changes, and update _homeState accordingly (.update)
                currentState.copy(
                    favorites = emptyList(),
                    recentSearches = emptyList()
                )
            }
        }
    }

    fun updateSearchTextFieldState(textFieldState: TextFieldState){
        //make a copy of the current state and change the searchQuery to the new query
        _homeState.update { it.copy(searchTextFieldState = textFieldState) }
    }

    fun addRecentSearch(location: NamedLocation) {
        _homeState.update {
            val updated = (listOf(location) + it.recentSearches)
                .distinct()
                .take(5)
            it.copy(recentSearches = updated)
        }
    }

    fun removeRecentSearch(location: NamedLocation) {
        _homeState.update {
            it.copy(recentSearches = it.recentSearches - location)
        }
    }

    fun onSearch(){
        val query = queryBuilder.buildQuery(_homeState.value.searchTextFieldState.text.toString())
        viewModelScope.launch {
            val results = locationRepository.getNames(query)
            _homeState.update { it.copy(searchResults = results) }
        }
    }
}