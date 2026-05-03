    package in2000.prosjekt.in2000project.ui.home

    import androidx.compose.foundation.BorderStroke
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.interaction.MutableInteractionSource
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.BoxWithConstraints
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.PaddingValues
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.layout.wrapContentHeight
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.LazyRow
    import androidx.compose.foundation.lazy.items
    import androidx.compose.material3.Card
    import androidx.compose.material3.Scaffold
    import androidx.compose.foundation.rememberScrollState
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.text.input.TextFieldState
    import androidx.compose.foundation.verticalScroll
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Phone
    import androidx.compose.material.icons.filled.Settings
    import androidx.compose.material3.Card
    import androidx.compose.material3.CardDefaults
    import androidx.compose.material3.DockedSearchBar
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.material3.ListItem
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.SearchBarDefaults
    import androidx.compose.material3.Surface
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.collectAsState
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.remember
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.platform.LocalFocusManager
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.lifecycle.compose.collectAsStateWithLifecycle
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.text.font.Font
    import androidx.compose.ui.text.font.FontFamily
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.font.FontWeight.Companion.Bold
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import androidx.navigation.NavGraph.Companion.findStartDestination
    import androidx.navigation.compose.rememberNavController
    import in2000.prosjekt.in2000project.R
    import in2000.prosjekt.in2000project.data.model.Coordinate
    import in2000.prosjekt.in2000project.data.model.NamedLocation
    import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
    import in2000.prosjekt.in2000project.data.model.enums.EmergencyServices
    import in2000.prosjekt.in2000project.navigation.Screen
    import in2000.prosjekt.in2000project.ui.map.NveViewModel
    import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
    import in2000.prosjekt.in2000project.ui.model.toUiModel
    import in2000.prosjekt.in2000project.ui.reusables.BottomBar
    import in2000.prosjekt.in2000project.ui.reusables.DangerLevelCard
    import in2000.prosjekt.in2000project.ui.reusables.SearchSection
    import in2000.prosjekt.in2000project.ui.reusables.TopBar
    import in2000.prosjekt.in2000project.ui.search.SearchViewModel
    import org.koin.compose.viewmodel.koinViewModel

    @Composable
    fun HomeScreen(
        modifier: Modifier = Modifier,
        onSettingsClick: () -> Unit,
        onNavigateToMap: (coordinate: Coordinate) -> Unit,
        searchViewModel: SearchViewModel = koinViewModel()
    ) {
        val focusManager = LocalFocusManager.current
        val searchState by searchViewModel.searchState.collectAsStateWithLifecycle()

        Scaffold(
            modifier = modifier,
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
                HomeTopBar(onSettingsClick)
                LogoSection()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                ){
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
                }

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

                    item {
                        EmergencyNumbersSection()
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
                    .padding(24.dp),
                contentScale = ContentScale.Fit
            )
        }
    }

    @Composable
    fun FavoriteSection(
        items: List<NamedLocation>,
        modifier: Modifier = Modifier,
        onNavigateToMap: (coordinate: Coordinate) -> Unit,
        nveViewModel: NveViewModel = viewModel()
    ) {

        // Temporary solution while we are waiting for the actual dangerlevels, so this part is very changeable
        val dangerCheckUiState by nveViewModel.dangerCheckUiState.collectAsState()

        LaunchedEffect(items) {
            if (items.isNotEmpty()) {
                // Checks the first favorite's location as an example.
                val firstLocation = items.first()
                nveViewModel.avalancheChecker(
                    firstLocation.coordinate.latitude,
                    firstLocation.coordinate.longitude
                )
            }
        }

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.favorites_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold

            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { location ->
                    LocationCard(
                        location,
                        onNavigateToMap = onNavigateToMap,
                        dangerLevel = dangerCheckUiState.dangerLevel,
                    )
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { location ->
                    LocationCard(
                        location,
                        onNavigateToMap = onNavigateToMap,
                        dangerLevel = null,
                    )
                }
            }
        }
    }

    @Composable
    fun LocationCard(
        location: NamedLocation,
        modifier: Modifier = Modifier,
        onNavigateToMap: (coordinate: Coordinate) -> Unit,
        dangerLevel: DangerLevelUiModel? = null,
    ) {

        Card(
            modifier = modifier
                .width(160.dp)
                .wrapContentHeight(),
            onClick = {
                onNavigateToMap(location.coordinate)
            },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = location.name,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (dangerLevel != null){
                    Text(
                        text = stringResource(dangerLevel.badgeLabelRes),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(dangerLevel.getColor())
                            .padding(
                                horizontal = 10.dp,
                                vertical = 4.dp
                            )
                    )
                }
            }
        }
    }

    @Composable
    fun HomeTopBar(
        onSettingsClick: () -> Unit,
        modifier: Modifier = Modifier
    ){
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    top = 16.dp,
                    start = 20.dp,
                    end = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.Center)
            )

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null)
            }
        }
    }

    @Composable
    fun EmergencyNumbersSection(
        modifier: Modifier = Modifier,
    ) {
        Surface(
            modifier = modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, Color.Red)
        ) {

            Row(
                modifier = Modifier
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(
                            end = 16.dp,
                            top = 4.dp
                        )
                )

                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.emergency_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    EmergencyServices.entries.forEach { service ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(service.service),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(service.number),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun HomeScreenPreview(
    ) {
        HomeScreen(
            onSettingsClick = {},
            onNavigateToMap = {},
        )
    }

