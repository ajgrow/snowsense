package in2000.prosjekt.in2000project.navigation

import com.mapbox.maps.ScreenCoordinate

sealed class Screen(
    val route: String
) {
    object Home : Screen("home")
    object Map : Screen("map/{coordinates}") {
        fun createRoute(coordinates: String) = "map/$coordinates"
    }
    object Info : Screen("info")
    object Settings : Screen("settings")

}