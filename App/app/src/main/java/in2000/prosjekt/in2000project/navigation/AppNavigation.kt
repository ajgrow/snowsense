package in2000.prosjekt.in2000project.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import in2000.prosjekt.in2000project.ui.home.HomeScreen
import in2000.prosjekt.in2000project.ui.information.InfoScreen
import in2000.prosjekt.in2000project.ui.map.MapScreenState
import in2000.prosjekt.in2000project.ui.settings.SettingScreen
import in2000.prosjekt.in2000project.ui.settings.SettingsViewModel
import in2000.prosjekt.in2000project.ui.theme.In2000projectTheme

@Composable
fun AppNavigation(){

    val navController = rememberNavController()

    val settingsViewModel: SettingsViewModel = viewModel()

    val themeMode = settingsViewModel.themeMode.value

    val textSizeMode = settingsViewModel.textSizeMode.value


    In2000projectTheme(
        themeMode = themeMode,
        textSizeMode = textSizeMode
    ) {
        NavHost(
            startDestination = Screen.Home.route,
            navController = navController,

            ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(
                route = Screen.Map.route,
                arguments = listOf(
                    navArgument("coordinates") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val coordinates = backStackEntry.arguments?.getString("coordinates")!!
                Log.d("fuckedupmap",coordinates)
                MapScreenState(
                    navController = navController,
                    coordinates = coordinates,
                    settingsViewModel = settingsViewModel)
            }
            composable(Screen.Info.route) {
                InfoScreen(navController = navController)
            }
            composable(Screen.Settings.route){
                SettingScreen(
                    navController = navController,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}