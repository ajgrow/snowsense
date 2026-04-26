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

    //initialize with mock data 
    init {
        viewModelScope.launch {
            _infoState.update { currentState ->
                currentState.copy(
                    title = "About the app",
                    teamTitle = "The Team",
                    aboutTeam = "We are a group of dedicated and creative IN2000 students working together to create an informative and user-friendly avalanche safety app. Our goal going into this was to create a reliable tool that helps users make safer decisions when traveling to potential avalanche-prone areas. This page is provides general information about avalanche risk levels and their meaning. Please return to the Homescreen or the Map for the main features",
                    avalancheTitle = "Avalanche Info",
                    avalancheGeneralInfo = "Avalanches have become increasingly more common as Global Climate Change continues to impact mountainous environments. When you're out exploring the country, it's important to stay informed and prioritize your safety. That's why we created this app. To help you make safer decisions and enjoy your time outdoors.",
                    dangerTitle = "Avalanche Danger Levels",
                    dangerLevels = listOf(
                        DangerLevel(
                            level = "1 - Low",
                            description = "Generally stable conditions.\n\nTriggering is generally possible only from high additional loads in isolated areas of very steep, extreme terrain. Only small and medium natural avalanches are possible.\n\nTypical danger signs: \nrarely any danger signs. \n\nForecast for around 20% of the winter season. Around 5% of avalanche fatalities."
                        ),
                        DangerLevel(
                            level = "2 - Moderate",
                            description = "Heightened avalanche conditions on specific terrain features.\n\nTriggering is possible, primarily from high additional loads, particularly on the indicated steep slopes. Very large natural avalanches are unlikely. \n\nTypical danger signs: \noften there are no danger signs, but that does not necessarily mean it is safe. Pay extra attention if persistent weak layer is one of the avalanche problems. \n\nForecast for around 50% of the winter season. Around 30% of avalanche fatalities."
                        ),
                        DangerLevel(
                            level = "3 - Considerable",
                            description = "Dangerous avalanche conditions.\n\nTriggering is possible, even from low additional loads, particularly on the indicated steep slopes. In certain situations some large, and in isolated cases very large natural avalanches are possible. \n\nTypical danger signs: \nrecent avalanche activity, cracking, and 'whumpf' sounds. Remote triggering of avalanches is typical, especially if persistent weak layer is one of the avalanche problems. \n\nForecast for around 30% of the winter season. Around 50% of avalanche fatalities."
                        ),
                        DangerLevel(
                            level = "4 - High",
                            description = "Very dangerous avalanche conditions. \n\nTriggering is likely, even from low additional loads, on many steep slopes. In some cases, numerous large and often very large natural avalanches can be expected. \n\nTypical danger signs: \nwidespread recent avalanche activity, cracking, and 'whumpf' sounds. \nRemote triggering of avalanches is typical. \n\nForecasted only on a few days throughout the winter. Around 10% of avalanche fatalities."
                        ),
                        DangerLevel(
                            level = "5 - Very High",
                            description = "Extraordinary avalanche conditions. \n\nNumerous very large and often extremely large natural avalanches can be expected, even in moderately steep terrain. \n\nVery rarely forecast."
                        )
                    )
                )
            }
        }
    }

    //We can add functions her later if we wish to update states in the infoscreen, but for now we have nothing we wish to change
}