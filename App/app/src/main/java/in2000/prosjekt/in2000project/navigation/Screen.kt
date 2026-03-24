package in2000.prosjekt.in2000project.navigation

sealed class Screen(
    val route: String
) {
    object Home : Screen("home")
    object Map : Screen("map")
    object Info : Screen("info")

}