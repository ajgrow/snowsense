package in2000.prosjekt.in2000project.ui.reusables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    navController: NavController
){
    CenterAlignedTopAppBar(
        modifier =  modifier,
        title = {Text(title)},
        actions = {
            IconButton(
                onClick = {navController.navigate(Screen.Settings.route)}
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}


@Preview
@Composable
fun TopBarPreview(
    modifier: Modifier = Modifier
){
    TopBar(modifier, "**title**", navController = rememberNavController())
}