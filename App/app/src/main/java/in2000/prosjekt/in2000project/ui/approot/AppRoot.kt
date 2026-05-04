package in2000.prosjekt.in2000project.ui.approot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.navigation.AppNavigation
import in2000.prosjekt.in2000project.navigation.NavigationActions
import in2000.prosjekt.in2000project.navigation.Screen
import in2000.prosjekt.in2000project.ui.map.MapStateHolder
import in2000.prosjekt.in2000project.ui.map.MapboxView
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.isBottomBarScreen
import in2000.prosjekt.in2000project.ui.settings.SettingsViewModel
import in2000.prosjekt.in2000project.ui.theme.In2000projectTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppRoot(
    modifier: Modifier = Modifier
) {
    val mapStateHolder = koinInject<MapStateHolder>()
    val settingsViewModel: SettingsViewModel = koinViewModel()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val navigationActions = NavigationActions(navController)

    In2000projectTheme(
        themeMode = settingsViewModel.themeMode,
        textScale = settingsViewModel.textScale,
    ) {
        val onMapRoute = currentDestination?.route == Screen.Map.route

        Scaffold(
            modifier = modifier,
            containerColor = Color.Transparent,
            bottomBar = {
                AnimatedVisibility(
                    visible = currentDestination.isBottomBarScreen(),
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                ) {
                    BottomBar(
                        currentRoute = currentDestination?.route,
                        onClick = navigationActions::navigateToTab
                    )
                }
            },
            contentWindowInsets = ScaffoldDefaults
                .contentWindowInsets
                .run {
                    if (currentDestination.isBottomBarScreen())
                        this
                    else
                        only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                }
        ) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                MapboxView(
                    mapStateHolder = mapStateHolder,
                    modifier = Modifier.fillMaxSize(),
                )

                if (!onMapRoute) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                }

                AppNavigation(
                    navController = navController,
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

    }
}