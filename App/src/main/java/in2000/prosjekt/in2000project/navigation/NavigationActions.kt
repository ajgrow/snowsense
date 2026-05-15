package in2000.prosjekt.in2000project.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Navigation actions for the app.
 *
 * @param navController The navigation controller.
 */
class NavigationActions(
    private val navController: NavController
) {
    fun navigateToTab(route: String) = navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = false
    }
}
