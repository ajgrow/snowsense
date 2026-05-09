package in2000.prosjekt.in2000project.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mapbox.geojson.Point
import in2000.prosjekt.in2000project.ui.screens.home.HomeScreen
import in2000.prosjekt.in2000project.ui.screens.information.InfoScreen
import in2000.prosjekt.in2000project.ui.screens.map.MapRoute
import in2000.prosjekt.in2000project.ui.screens.map.MapStateHolder
import in2000.prosjekt.in2000project.ui.viewmodels.RiskViewModel
import in2000.prosjekt.in2000project.ui.viewmodels.SearchViewModel
import in2000.prosjekt.in2000project.screens.settings.SettingScreen
import in2000.prosjekt.in2000project.ui.viewmodels.SettingsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
){
    val navigationActions = NavigationActions(navController)
    val mapStateHolder: MapStateHolder = koinInject()

    val searchViewModel = koinViewModel<SearchViewModel>()
    val riskViewModel = koinViewModel<RiskViewModel>()

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
                onNavigateToMap = { coordinate ->
                    mapStateHolder.flyToSearch(
                        Point.fromLngLat(
                            coordinate.longitude,
                            coordinate.latitude)
                    )
                    navigationActions.navigateToTab(Screen.Map.route) },
                searchViewModel = searchViewModel,
                riskViewModel = riskViewModel
            )
        }

        composable(Screen.Map.route) {
            MapRoute(searchViewModel = searchViewModel)
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