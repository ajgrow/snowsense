package in2000.prosjekt.in2000project.ui.information

import androidx.lifecycle.ViewModel
import in2000.prosjekt.in2000project.ui.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


data class InfoUiState(
    val dangers: List<String> = emptyList()
)

class InfoViewModel: ViewModel(){
    private val _infoState = MutableStateFlow(HomeUiState())
    val infoState: StateFlow<HomeUiState> = _infoState.asStateFlow()

}