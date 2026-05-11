package in2000.prosjekt.in2000project.ui.screens.information

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import in2000.prosjekt.in2000project.domain.model.enums.DangerLevel
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InfoUiState(
    val dangerLevels: List<DangerLevelUiModel> = emptyList()
)

class InfoViewModel(application: Application): AndroidViewModel(application){

    private val _infoUiState = MutableStateFlow(InfoUiState())
    val infoUiState: StateFlow<InfoUiState> = _infoUiState.asStateFlow()

    init {
        // We perform the mapping from Domain to UI model here in the ViewModel.
        // This keeps the Composable "dumb" and ensures presentation logic is centralized.
        _infoUiState.value = InfoUiState(
            dangerLevels = DangerLevel.entries.map { it.toUiModel() }
        )
    }
}
