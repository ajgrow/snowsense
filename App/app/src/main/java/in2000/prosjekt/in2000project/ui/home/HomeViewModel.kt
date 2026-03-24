package in2000.prosjekt.in2000project.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val seachQuery: String = "",
    val favorites: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList()
)

class HomeViewModel: ViewModel(){

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()
    

}