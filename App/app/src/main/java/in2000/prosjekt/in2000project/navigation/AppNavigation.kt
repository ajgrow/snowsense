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
import in2000.prosjekt.in2000project.ui.map.MapRoute
import in2000.prosjekt.in2000project.ui.map.MapScreenState
import in2000.prosjekt.in2000project.ui.search.SearchViewModel
import in2000.prosjekt.in2000project.ui.settings.SettingScreen
import in2000.prosjekt.in2000project.ui.settings.SettingsViewModel
import in2000.prosjekt.in2000project.ui.theme.In2000projectTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    val settingsViewModel: SettingsViewModel = koinViewModel()

    val searchViewModel: SearchViewModel = koinViewModel()

    val themeMode = settingsViewModel.themeMode

    val textScale = settingsViewModel.textScale


    In2000projectTheme(
        themeMode = themeMode,
        textScale = textScale
    ) {
        NavHost(
            startDestination = Screen.Home.route,
            navController = navController,

            ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController, searchViewModel = searchViewModel)
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
                MapRoute(
                    navController = navController,
                    coordinates = coordinates,
                    searchViewModel = searchViewModel)
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