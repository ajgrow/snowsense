    package in2000.prosjekt.in2000project.ui.home

    import androidx.compose.foundation.Image
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.interaction.MutableInteractionSource
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.BoxWithConstraints
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.PaddingValues
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.layout.wrapContentHeight
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.LazyRow
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.text.input.TextFieldState
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material3.Card
    import androidx.compose.material3.DockedSearchBar
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.ListItem
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.SearchBarDefaults
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.saveable.rememberSaveable
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.focus.onFocusChanged
    import androidx.compose.ui.platform.LocalFocusManager
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.lifecycle.compose.collectAsStateWithLifecycle
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.res.stringResource
    import androidx.navigation.NavController
    import androidx.navigation.NavGraph.Companion.findStartDestination
    import androidx.navigation.compose.rememberNavController
    import in2000.prosjekt.in2000project.R
    import in2000.prosjekt.in2000project.data.model.Coordinate
    import in2000.prosjekt.in2000project.data.model.NamedLocation
    import in2000.prosjekt.in2000project.navigation.Screen
    import in2000.prosjekt.in2000project.ui.reusables.BottomBar
    import in2000.prosjekt.in2000project.ui.reusables.SearchSection
    import in2000.prosjekt.in2000project.ui.reusables.TopBar
    import in2000.prosjekt.in2000project.ui.search.SearchViewModel
    import org.koin.compose.viewmodel.koinViewModel

    @Composable
    fun HomeScreen(
        modifier: Modifier = Modifier,
        navController: NavController,
        searchViewModel: SearchViewModel = koinViewModel()
    ) {
        val focusManager = LocalFocusManager.current
        val searchState by searchViewModel.searchState.collectAsStateWithLifecycle()

        Scaffold(
            modifier = modifier,
            topBar = { TopBar(
                title = stringResource(R.string.app_name),
                navController = navController) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                       focusManager.clearFocus()
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LogoSection()

                val onNavigateToMap = { coordinate: Coordinate ->
                    val coordinateString = "${coordinate.longitude},${coordinate.latitude}"
                    navController.navigate(Screen.Map.createRoute(coordinateString)) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = false
                        restoreState = false
                    }
                }

                SearchSection(
                    //get states from the viewmodel
                    modifier = Modifier,
                    textFieldState = searchState.searchTextFieldState,
                    onSearch = { searchViewModel.onSearch() },
                    searchResults = searchState.searchResults,
                    onNavigateToMap = onNavigateToMap,
                    onSelectResult = searchViewModel::addRecentSearch,
                    recentSearches = searchState.recentSearches
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),

                    contentPadding = PaddingValues(16.dp),          //adds padding around content
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        FavoriteSection(
                            items = searchState.favorites,
                            modifier = Modifier,
                            onNavigateToMap = onNavigateToMap
                        )
                    }

                    item {
                        RecentSection(
                            items = searchState.recentSearches,
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
        BoxWithConstraints(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            //Take 45% of available width and caps the value at 240dp max (coerceAtMost)
            //This enables the logo to adjust to different screensizes,
            //without becoming ridiculously large on big screens
            val logoWidth = (maxWidth * 0.45f).coerceAtMost(240.dp)

            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .width(logoWidth)
                    .wrapContentHeight()
                    .padding(10.dp),
                contentScale = ContentScale.Fit
            )
        }
    }



    @Composable
    fun FavoriteSection(
        items: List<NamedLocation>,
        modifier: Modifier = Modifier,
        onNavigateToMap: (coordinate: Coordinate) -> Unit
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
        onNavigateToMap: (coordinate: Coordinate) -> Unit
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
        onNavigateToMap: (coordinate: Coordinate) -> Unit
    ) {
        Card(
            modifier = modifier,
            onClick = {
                onNavigateToMap(location.coordinate)
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
    ) {
        HomeScreen(navController = rememberNavController())
    }
