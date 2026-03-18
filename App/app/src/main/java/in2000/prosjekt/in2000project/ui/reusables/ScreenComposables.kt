package in2000.prosjekt.in2000project.ui.reusables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun BottomBar(
    modifier: Modifier = Modifier
) {
    NavigationBar() {
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
fun BottomBarPreview(
    modifier: Modifier = Modifier
){
    BottomBar(modifier)
}