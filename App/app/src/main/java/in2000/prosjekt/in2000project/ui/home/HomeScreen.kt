package in2000.prosjekt.in2000project.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Card
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.homeState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = { TopBar(title = "Home") },
        bottomBar = { BottomBar(modifier, navController) }
    ) { paddingValues ->
        // Using LazyColumn for the whole screen ensures everything is scrollable
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),          //adds padding around content
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //All content should go here
            item { LogoSection() }

            item {
                SearchSection(
                    //get states from the viewmodel
                    query = uiState.searchQuery,  
                    onQueryChange = { newText -> 
                        // newText is the updated string passed from OutlinedTextField's onValueChange in SearchSection
                        homeViewModel.updateSearchQuery(newText) 
                    },
                    onSearchClick = { homeViewModel.onSearch() }
                )
            }
            item { FavoriteSection(items = uiState.favorites) }
            item { RecentSection(items = uiState.recentSearches) }
        }
    }
}

@Composable
fun LogoSection(
    modifier: Modifier = Modifier
) {
    Image(
        painterResource(R.drawable.logo_placeholder),
        contentDescription = "App Logo",
        modifier = modifier.size(130.dp)
    )
}

@Composable
fun SearchSection(
    query: String,
    onSearchClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    //We should add some rules to the searchSection, like restrictions on type of input and input length
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search for area...") },
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
) {
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
            items(items) { area ->
                AreaCard(area)
            }
        }
    }
}

@Composable
fun RecentSection(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Recent searches",
            style = MaterialTheme.typography.titleMedium
        )

        items.forEach { area ->
            AreaCard(area)
        }

    }
}

@Composable
fun AreaCard(
    area: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
    ) {
        Text(
            text = area,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview(
    modifier: Modifier = Modifier
) {
    HomeScreen(navController = rememberNavController())
}
