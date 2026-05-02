package in2000.prosjekt.in2000project.ui.reusables

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.navigation.Screen

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    NavigationBar(
        modifier = modifier.height(60.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { navController.navigateSingleTopTo(Screen.Home.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(R.string.nav_home_desc),
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Map.route,
            onClick = { navController.navigateSingleTopTo(Screen.Map.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = stringResource(R.string.nav_map_desc),
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Info.route,
            onClick = { navController.navigateSingleTopTo(Screen.Info.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.nav_info_desc),
                )
            }
        )
    }
}

private fun NavController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(Screen.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

@Preview
@Composable
fun BottomBarPreview(
    modifier: Modifier = Modifier,
){
    BottomBar(modifier, navController = rememberNavController())
}