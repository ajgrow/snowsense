package in2000.prosjekt.in2000project.ui.information

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.R
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DangerLevel(
    val level: String,
    val description: String
)

data class InfoUiState(
    val title : String = "",
    val teamTitle: String = "",
    val aboutTeam: String = "",
    val avalancheTitle: String = "",
    val avalancheGeneralInfo: String = "",
    val dangerTitle: String = "",
    val dangerLevels: List<DangerLevel> = emptyList()
)

class InfoViewModel(application: Application): AndroidViewModel(application){
    private val context = getApplication<Application>()
    private val _infoState = MutableStateFlow(InfoUiState())
    val infoState: StateFlow<InfoUiState> = _infoState.asStateFlow()

    //initialize with mock data 
    init {
        viewModelScope.launch {
            _infoState.update { currentState ->
                currentState.copy(
                    title = context.getString(R.string.about_app_title),
                    teamTitle = context.getString(R.string.team_title),
                    aboutTeam = context.getString(R.string.about_team_text),
                    avalancheTitle = context.getString(R.string.avalanche_info_title),
                    avalancheGeneralInfo = context.getString(R.string.avalanche_general_info_text),
                    dangerTitle = context.getString(R.string.danger_levels_title),
                    dangerLevels = listOf(
                        DangerLevel(
                            level = context.getString(R.string.danger_level_1_title),
                            description = context.getString(R.string.danger_level_1_description)
                        ),
                        DangerLevel(
                            level = context.getString(R.string.danger_level_2_title),
                            description = context.getString(R.string.danger_level_2_description)
                        ),
                        DangerLevel(
                            level = context.getString(R.string.danger_level_3_title),
                            description = context.getString(R.string.danger_level_3_description)
                        ),
                        DangerLevel(
                            level = context.getString(R.string.danger_level_4_title),
                            description = context.getString(R.string.danger_level_4_description)
                        ),
                        DangerLevel(
                            level = context.getString(R.string.danger_level_5_title),
                            description = context.getString(R.string.danger_level_5_description)
                        )
                    )
                )
            }
        }
    }

    //We can add functions her later if we wish to update states in the infoscreen, but for now we have nothing we wish to change
}