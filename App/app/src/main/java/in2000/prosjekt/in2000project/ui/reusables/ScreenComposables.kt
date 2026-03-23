package in2000.prosjekt.in2000project.ui.reusables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String
){
    CenterAlignedTopAppBar(
        modifier =  modifier,
        title = {Text(title)},
        actions = {
            IconButton(
                onClick = {/*TODO*/}
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    NavigationBar(
        modifier = modifier
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = {navController.navigate(Screen.Home.route){
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }},
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Go to home screen",
                )
            }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Map.route,
            onClick = {navController.navigate(Screen.Map.route){
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }},
            icon = {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Go to map screen",
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = {/*TODO*/},
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Go to information screen",
                )
            },
        )
    }
}


@Preview
@Composable
fun TopBarPreview(
    modifier: Modifier = Modifier
){
    TopBar(modifier, "**title**")
}

@Preview
@Composable
fun BottomBarPreview(
    modifier: Modifier = Modifier,
){
    BottomBar(modifier, navController = rememberNavController())
}