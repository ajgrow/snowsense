package in2000.prosjekt.in2000project.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import in2000.prosjekt.in2000project.R
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.TopBar
import androidx.compose.foundation.lazy.items
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController : NavController
){
    //Used in SearchSection. Should be moved to Viewmodel?
    var query by rememberSaveable { mutableStateOf("") }

    // Mock data: these should eventually be moved to a ViewModel
    val favorites = listOf("Jotunheimen", "Rondane")
    val recents = listOf("Jotunheimen", "Rondane", "Romsdalen", "Hardangervidda", "Lofoten")

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
            Logo()
            SearchSection(
                query = query,
                onQueryChange = {query = it},
                onSearchClick = {/*TODO*/})
            FavoriteSection(items = favorites)
            RecentSection(items = recents)
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

@Composable
fun SearchSection(
    query: String,
    onSearchClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
){

    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = query,
        onValueChange = onQueryChange,
        placeholder = {Text("Search for area...")},
        singleLine = true,
        shape = RoundedCornerShape(50.dp),
        trailingIcon = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        }
    )
}

@Composable
fun FavoriteSection(
    items: List<String>,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Favorites",
            style = MaterialTheme.typography.titleMedium
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items){
                area -> Text(area)
            }
        }
    }
}

@Composable
fun RecentSection(
    items: List<String>,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Recent searches",
            style = MaterialTheme.typography.titleMedium
        )

        items.forEach{
            area -> Text(area)
        }

    }
}




@Preview
@Composable
fun HomeScreenPreview(
    modifier: Modifier = Modifier
){
    HomeScreen(navController = rememberNavController())
}