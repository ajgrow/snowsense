package in2000.prosjekt.in2000project.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mapbox.geojson.Point
import in2000.prosjekt.in2000project.ui.risk.RiskViewModel
import in2000.prosjekt.in2000project.ui.screen.home.HomeRoute
import in2000.prosjekt.in2000project.ui.screen.information.InfoRoute
import in2000.prosjekt.in2000project.ui.screen.map.MapRoute
import in2000.prosjekt.in2000project.ui.screen.map.MapStateHolder
import in2000.prosjekt.in2000project.ui.screen.settings.SettingsRoute
import in2000.prosjekt.in2000project.ui.screen.settings.SettingsViewModel
import in2000.prosjekt.in2000project.ui.search.SearchViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Holds the NavHost for the app, wiring shared ViewModels.
 *
 * @param navController The navigation controller.
 * @param settingsViewModel The settings view model.
 */
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
            HomeRoute(
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
            MapRoute(
                mapStateHolder = mapStateHolder,
                searchViewModel = searchViewModel,
                riskViewModel = riskViewModel
            )
        }

        composable(Screen.Info.route) {
            InfoRoute(
                onSettingsClick = { navController
                    .navigate(Screen
                        .Settings.route) })
        }

        composable(Screen.Settings.route) {
            SettingsRoute(
                onBackClick = navController::popBackStack,
                settingsViewModel = settingsViewModel
            )
        }
    }
}