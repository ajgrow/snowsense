package in2000.prosjekt.in2000project.ui.reusables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {/*TODO*/},
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Go to home screen",
                )
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = {/*TODO*/},
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
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Go to notifications screen",
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
    modifier: Modifier = Modifier
){
    BottomBar(modifier)
}