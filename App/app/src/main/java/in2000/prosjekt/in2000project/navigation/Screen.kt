package in2000.prosjekt.in2000project.navigation

sealed class Screen(
    val route: String
) {
    data object Home : Screen("home")
    data object Map : Screen("map")
    data object Info : Screen("info")
    data object Settings : Screen("settings")
}