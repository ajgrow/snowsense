package in2000.prosjekt.in2000project.ui.information

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
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

class InfoViewModel: ViewModel(){
    private val _infoState = MutableStateFlow(InfoUiState())
    val infoState: StateFlow<InfoUiState> = _infoState.asStateFlow()
    
    init {
        viewModelScope.launch {
            _infoState.update { currentState ->
                currentState.copy(
                    title = "About the app",
                    teamTitle = "The Team",
                    aboutTeam = "We are a group of incredibly talented, wonderfully creative, and pretty f***ing cool IN2000 students. We have been tasked with making the coooooooolest app you've ever seen and that's what we plan to do. This page is just FYI, but click back to Home or the Map for more :)",
                    avalancheTitle = "Avalanche Info",
                    avalancheGeneralInfo = "Avalanches have become increasingly more common as Global Climate Change keeps on keeping on. So when you're out shreddin', you gots ta be careful. That's why we created this app! We hope you enjoy it.",
                    dangerTitle = "Avalanche Danger Levels",
                    dangerLevels = listOf(
                        DangerLevel(
                            level = "1 - Low",
                            description = "NICE! This means generally safe avalanche conditions. Natural and human-triggered avalanches are unlikely. GO! GO! GO!"
                        ),
                        DangerLevel(
                            level = "2 - Moderate",
                            description = "Hmmm... maybe think twice before taking this on? Heightened avalanche conditions on some terrain features. Human-triggered avalanches are definitely possible."
                        ),
                        DangerLevel(
                            level = "3 - Considerable",
                            description = "YIKES, POSSIBLE DEATH - Dangerous avalanche conditions. PLEASE DON'T GO! Human-triggered avalanches are likely and natural avalanches are possible."
                        ),
                        DangerLevel(
                            level = "4 - High",
                            description = "DEATH IS LIKELY - DO NOT GO!! Very dangerous avalanche conditions. Human-triggered avalanches are very likely and natural avalanches are likely."
                        ),
                        DangerLevel(
                            level = "5 - Extreme",
                            description = "CERTAIN DEATH!! At this point, it might be time to book a flight south. Avoid all avalanche terrain. Large natural avalanches are certain or nearly certain."
                        )
                    )
                )
            }
        }
    }

    //We can add functions her later if we wish to update states in the infoscreen, but for now we have nothing we wish to change
}