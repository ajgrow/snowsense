package in2000.prosjekt.in2000project.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val searchQuery: String = "",
    val favorites: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList()
)

class HomeViewModel: ViewModel(){

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    //initialize with mock data for the MVP:
    init{
        viewModelScope.launch {
            //Once we have a repository in place, we will call it here
            //example: val data = HomeRepository.getData()
            _homeState.update { currentState ->
                //make a new copy of currentState (HomeUiState) with these changes, and update _homeState accordingly (.update)
                currentState.copy(
                    favorites = listOf("Jotunheimen", "Rondane"),
                    recentSearches = listOf("Jotunheimen", "Rondane", "Romsdalen", "Hardangervidda", "Lofoten", "Vesterålen")
                )
            }
        }
    }

    fun updateSearchQuery(query: String){
        _homeState.update { it.copy(searchQuery = query) }
    }

    fun onSearch(){
        //TODO: Implement logic for search function 
    }
}