package in2000.prosjekt.in2000project.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import in2000.prosjekt.in2000project.ui.home.HomeScreen
import in2000.prosjekt.in2000project.ui.information.InfoScreen
import in2000.prosjekt.in2000project.ui.map.MapScreen
import in2000.prosjekt.in2000project.ui.map.MapScreenState
import in2000.prosjekt.in2000project.ui.settings.SettingScreen

@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    NavHost(
        startDestination = Screen.Home.route,
        navController = navController,

    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
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
            MapScreenState(navController = navController, coordinates = coordinates)
        }
        composable(Screen.Info.route) {
            InfoScreen(navController = navController)
        }
        composable(Screen.Settings.route){
            SettingScreen(navController = navController)
        }
    }
}