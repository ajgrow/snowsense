package in2000.prosjekt.in2000project.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.TopBar

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier

){
    Scaffold(
        topBar = {TopBar(title ="Home")},
        bottomBar = {BottomBar()}
    ) {
        paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)){
            //All content should go here
            Logo()
            Text("**TODO: ADD SEARCHBAR**")
            Text("**TODO: ADD FAVORITES**")
            Text("**TODO: ADD PREVIOUS SEARCHES**")
        }
    }
}

@Composable
fun Logo(
    modifier: Modifier = Modifier
){
    Image(
        painterResource(R.drawable.logo_placeholder),
        contentDescription = "Placeholder for Logo"
    )
}

@Preview
@Composable
fun HomeScreenPreview(
    modifier: Modifier = Modifier
){
    HomeScreen()
}