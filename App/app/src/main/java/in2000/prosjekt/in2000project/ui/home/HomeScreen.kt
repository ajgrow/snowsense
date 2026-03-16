package in2000.prosjekt.in2000project.ui.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier

){
    Text("Hello World")
}

@Preview
@Composable
fun HomeScreenPreview(
    modifier: Modifier = Modifier
){
    HomeScreen()
}