package in2000.prosjekt.in2000project.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.TopBar

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    Scaffold(
        topBar = {TopBar(title ="Home")},
        bottomBar = {BottomBar(modifier, navController)}
    ) {
            paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)){
            //All content should go here
            Text("**TODO: ADD MAP**")
        }
    }
}

@Preview
@Composable
fun MapScreenPreview() {
    MapScreen(navController = rememberNavController())
}