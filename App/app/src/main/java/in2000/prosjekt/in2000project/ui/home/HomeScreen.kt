package in2000.prosjekt.in2000project.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import in2000.prosjekt.in2000project.data.model.NamedLocation
import in2000.prosjekt.in2000project.navigation.Screen

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.homeState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                title = stringResource(R.string.home_title),
                navController = navController)
                 },
        bottomBar = { BottomBar(modifier, navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoSection()

            val onNavigateToMap = { coordinate: String ->
                navController.navigate(Screen.Map.createRoute(coordinate)) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = false
                    restoreState = false
                }
            }

            SearchSection(
                //get states from the viewmodel
                modifier = Modifier,
                textFieldState = uiState.searchTextFieldState,
                onSearch = { homeViewModel.onSearch() },
                searchResults = uiState.searchResults,
                onNavigateToMap = onNavigateToMap,
                onSelectResult = { result ->
                    homeViewModel.addRecentSearch(result)
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),

                contentPadding = PaddingValues(16.dp),          //adds padding around content
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    FavoriteSection(
                        items = uiState.favorites,
                        modifier = Modifier,
                        onNavigateToMap = onNavigateToMap
                    )
                }

                item {
                    RecentSection(
                        items = uiState.recentSearches,
                        modifier = Modifier,
                        onNavigateToMap = onNavigateToMap
                    )
                }
            }
        }
    }
}

@Composable
fun LogoSection(
    modifier: Modifier = Modifier
) {
    Image(
        painterResource(R.drawable.logo_placeholder),
        contentDescription = stringResource(R.string.app_logo_description),
        modifier = modifier.size(130.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSection(
    textFieldState : TextFieldState,
    searchResults: List<NamedLocation>,
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit,
    onSelectResult: (location: NamedLocation) -> Unit,
    onNavigateToMap: (coordinate: String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
        //We should add some rules to the searchSection, like restrictions on type of input and input length
        DockedSearchBar(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            inputField = {
                SearchBarDefaults.InputField(
                    query = textFieldState.text.toString(),
                    onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                    onSearch = {
                        onSearch(textFieldState.text.toString())
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = {
                        Text(stringResource(R.string.search_placeholder)) }
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                searchResults.forEach { result ->
                    ListItem(
                        headlineContent = { Text(result.name) },
                        modifier = Modifier
                            .clickable {
                                onNavigateToMap("${result.coordinate.longitude},${result.coordinate.latitude}")
                                onSelectResult(result)
                                textFieldState.edit { replace(0, length, result.name) }
                                expanded = false
                            }
                            .fillMaxWidth()
                    )
                }
            }
    }
}

@Composable
fun FavoriteSection(
    items: List<NamedLocation>,
    modifier: Modifier = Modifier,
    onNavigateToMap: (coordinate: String) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.favorites_title),
            style = MaterialTheme.typography.titleMedium
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { location ->
                LocationCard(location, onNavigateToMap = onNavigateToMap)
            }
        }
    }
}

@Composable
fun RecentSection(
    items: List<NamedLocation>,
    modifier: Modifier = Modifier,
    onNavigateToMap: (coordinate: String) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.recent_searches_title),
            style = MaterialTheme.typography.titleMedium
        )

        items.forEach { location ->
            LocationCard(
                location,
                onNavigateToMap = onNavigateToMap
            )
        }
    }
}

@Composable
fun LocationCard(
    location: NamedLocation,
    modifier: Modifier = Modifier,
    onNavigateToMap: (coordinate: String) -> Unit
) {
    Card(
        modifier = modifier,
        onClick = {
            onNavigateToMap("${location.coordinate.longitude},${location.coordinate.latitude}")
        }
    ) {
        Text(
            text = location.name,
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
