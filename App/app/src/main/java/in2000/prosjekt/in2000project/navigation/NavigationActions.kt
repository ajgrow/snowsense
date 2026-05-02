package in2000.prosjekt.in2000project.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import in2000.prosjekt.in2000project.data.model.Coordinate

class NavigationActions(
    private val navController: NavController
) {
    fun navigateToTab(route: String)
        = navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true }
            launchSingleTop = true
            restoreState = true
        }

    fun navigateToMapCoordinates(coordinate: Coordinate)
            = navController
        .navigate(
            Screen.Map.createRoute(
                "${coordinate.longitude},${coordinate.latitude}")
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true }
            launchSingleTop = false
            restoreState = false
        }
}
