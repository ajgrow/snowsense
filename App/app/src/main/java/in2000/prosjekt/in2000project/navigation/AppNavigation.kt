package in2000.prosjekt.in2000project.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import in2000.prosjekt.in2000project.ui.home.HomeScreen
import in2000.prosjekt.in2000project.ui.information.InfoScreen
import in2000.prosjekt.in2000project.ui.map.MapRoute
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.search.SearchViewModel
import in2000.prosjekt.in2000project.ui.settings.SettingScreen
import in2000.prosjekt.in2000project.ui.settings.SettingsViewModel
import in2000.prosjekt.in2000project.ui.theme.In2000projectTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationActions = NavigationActions(navController)

    val settingsViewModel: SettingsViewModel = viewModel()

    val searchViewModel: SearchViewModel = koinViewModel()

    In2000projectTheme(
        themeMode = settingsViewModel.themeMode,
        textScale = settingsViewModel.textScale,
    ) {
        Scaffold(
            bottomBar = { BottomBar(
                currentRoute = currentRoute,
                onClick = navigationActions
                    ::navigateToTab
            ) }
        ) { innerPadding ->
            NavHost(
                modifier = Modifier.padding(innerPadding),
                startDestination = Screen.Home.route,
                navController = navController
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
                        navController = navController,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}