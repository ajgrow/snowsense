package in2000.prosjekt.in2000project.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.navigation.Screen

/**
 * Bottom navigation bar for the app.
 *
 * @param modifier The modifier to apply to the bottom bar.
 * @param currentRoute The current route.
 * @param onClick The callback to invoke when a navigation item is clicked.
 */
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    currentRoute: String?,
    onClick: (String) -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        windowInsets = WindowInsets.navigationBars
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { onClick(Screen.Home.route) } ,
            icon = { Icon(
                imageVector = Icons.Default.Home,
                contentDescription = stringResource(R.string.nav_home_desc),
                ) }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Map.route,
            onClick = { onClick(Screen.Map.route) },
            icon = { Icon(
                imageVector = Icons.Default.Place,
                contentDescription = stringResource(R.string.nav_map_desc),
                ) }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Info.route,
            onClick = { onClick(Screen.Info.route) },
            icon = { Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.nav_info_desc),
                ) }
        )
    }
}

@Preview
@Composable
fun BottomBarPreview(
    modifier: Modifier = Modifier,
){
    BottomBar(
        modifier,
        currentRoute = Screen.Home.route,
        onClick = {}
    )
}

internal fun NavDestination?.isBottomBarScreen()
    = this?.route in setOf(
    Screen.Home.route,
    Screen.Map.route,
    Screen.Info.route,
)