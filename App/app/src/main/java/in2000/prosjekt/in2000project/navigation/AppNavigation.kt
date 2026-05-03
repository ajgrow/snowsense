package in2000.prosjekt.in2000project.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import in2000.prosjekt.in2000project.ui.home.HomeScreen
import in2000.prosjekt.in2000project.ui.information.InfoScreen
import in2000.prosjekt.in2000project.ui.map.MapRoute
import in2000.prosjekt.in2000project.ui.search.SearchViewModel
import in2000.prosjekt.in2000project.ui.settings.SettingScreen
import in2000.prosjekt.in2000project.ui.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
){
    val navigationActions = NavigationActions(navController)

    val searchViewModel = koinViewModel<SearchViewModel>()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSettingsClick = { navController
                    .navigate(Screen
                        .Settings.route) },
                onNavigateToMap = navigationActions
                ::navigateToMapCoordinates,
                searchViewModel = searchViewModel
            )
        }

        composable(
            route = Screen.Map.route,
            arguments = listOf(
                navArgument("coordinates") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val coordinates = backStackEntry.arguments
                ?.getString("coordinates")!!
            MapRoute(
                coordinates = coordinates,
                searchViewModel = searchViewModel
            )
        }

        composable(Screen.Info.route) {
            InfoScreen(
                onSettingsClick = { navController
                    .navigate(Screen
                        .Settings.route) })
        }

        composable(Screen.Settings.route) {
            SettingScreen(
                onBackClick = navController::popBackStack,
                settingsViewModel = settingsViewModel
            )
        }
    }
}