package in2000.prosjekt.in2000project.ui.screens.home

    import androidx.compose.animation.AnimatedVisibility
    import androidx.compose.animation.expandVertically
    import androidx.compose.animation.fadeIn
    import androidx.compose.animation.fadeOut
    import androidx.compose.animation.shrinkVertically
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.interaction.MutableInteractionSource
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.BoxWithConstraints
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.PaddingValues
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.offset
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.layout.wrapContentHeight
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.LazyRow
    import androidx.compose.foundation.lazy.items
    import androidx.compose.material3.Card
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Settings
    import androidx.compose.material3.CardDefaults
    import androidx.compose.material.icons.filled.Close
    import androidx.compose.material3.CircularProgressIndicator
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.material3.MaterialTheme
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
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.font.FontWeight.Companion.Bold
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.ui.unit.sp
    import in2000.prosjekt.in2000project.R
    import in2000.prosjekt.in2000project.domain.model.Coordinate
    import in2000.prosjekt.in2000project.domain.model.NamedLocation
    import in2000.prosjekt.in2000project.ui.viewmodels.RiskUiState
    import in2000.prosjekt.in2000project.ui.viewmodels.RiskViewModel
    import in2000.prosjekt.in2000project.ui.components.EmergencySection
    import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
    import in2000.prosjekt.in2000project.ui.components.SearchSection
    import in2000.prosjekt.in2000project.ui.viewmodels.SearchViewModel
    import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    onNavigateToMap: (coordinate: Coordinate) -> Unit,
    searchViewModel: SearchViewModel = koinViewModel(),
    riskViewModel: RiskViewModel = koinViewModel()
) {
    val focusManager = LocalFocusManager.current
    val searchState by searchViewModel.searchState.collectAsStateWithLifecycle()
    val riskState by riskViewModel.riskState.collectAsStateWithLifecycle()

    val favorites = searchState.favorites

    LaunchedEffect(Unit) {
        searchViewModel.clearQuery()
    }

    LaunchedEffect(favorites) {
        riskViewModel.refreshFavoriteRisks(favorites)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                   focusManager.clearFocus()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeTopBar(onSettingsClick)
            AnimatedVisibility(
                visible = !searchState.isSearchActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LogoSection()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
            ){
                SearchSection(
                    //get states from the viewmodel
                    modifier = Modifier,
                    searchState = searchState,
                    onNavigateToMap = onNavigateToMap,
                    onQueryChange = searchViewModel::onQueryChange,
                    onSelectResult = searchViewModel::selectSearchResult,
                    onActiveChange = searchViewModel::setSearchActive
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 0.dp,
                    end = 0.dp,
                    top = 16.dp,
                    bottom = 0.dp
                ), //adds padding around content
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    FavoriteSection(
                        items = searchState.favorites,
                        modifier = Modifier,
                        onNavigateToMap = onNavigateToMap,
                        onRemoveLocation = searchViewModel::removeFavoriteLocation,
                        riskState = riskState
                    )
                }

                item {
                    RecentSection(
                        items = searchState.recentSearches,
                        modifier = Modifier,
                        onNavigateToMap = onNavigateToMap,
                        onRemoveLocation = searchViewModel::removeRecentSearch
                    )
                }

                item {
                    EmergencySection(
                        modifier = Modifier.padding(horizontal = 16.dp)
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
        val logoWidth = (maxWidth * 0.45f).coerceAtMost(240.dp)

        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = stringResource(R.string.app_logo_description),
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
    onRemoveLocation: (NamedLocation) -> Unit,
    riskState: RiskUiState
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.favorites_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { location ->
                val locationRiskState = riskState.locationRisks[location.coordinate]
                LocationCard(
                    location,
                    onNavigateToMap = onNavigateToMap,
                    dangerLevel = locationRiskState?.dangerLevel,
                    isLoading = locationRiskState?.isLoading == true,
                    onRemoveLocation = onRemoveLocation,
                    showDangerLevel = true,
                    removeButtonAtBottom = true
                )
            }
        }
    }
}

@Composable
fun RecentSection(
    items: List<NamedLocation>,
    modifier: Modifier = Modifier,
    onNavigateToMap: (coordinate: Coordinate) -> Unit,
    onRemoveLocation: (NamedLocation) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.recent_searches_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { location ->
                LocationCard(
                    location,
                    onNavigateToMap = onNavigateToMap,
                    onRemoveLocation = onRemoveLocation,
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
    onRemoveLocation: (NamedLocation) -> Unit,
    showDangerLevel: Boolean = false,
    dangerLevel: DangerLevelUiModel? = null,
    isLoading: Boolean = false,
    removeButtonAtBottom: Boolean = false,
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .wrapContentHeight()
    ) {
        Card(
            modifier = Modifier,
            onClick = {
                onNavigateToMap(location.coordinate)
            },
            shape = RoundedCornerShape(16.dp),
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
                    fontWeight = Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (showDangerLevel) {
                    val label = dangerLevel?.badgeLabelRes?.let { stringResource(it) } ?: ""

                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(50))
                            .background(dangerLevel?.getColor() ?: Color.Transparent)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
        IconButton(
            onClick = { onRemoveLocation(location) },
            modifier = Modifier
                .align(if (removeButtonAtBottom) Alignment.BottomEnd else Alignment.CenterEnd)
                .offset(x = (-12).dp, y = if (removeButtonAtBottom) (-8).dp else 0.dp)
                .size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.remove),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(30.dp)
            )
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
                contentDescription = stringResource(R.string.open_settings))
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview(){
    HomeScreen(
        onNavigateToMap = {},
        onSettingsClick = {},
        searchViewModel = viewModel()
    )
}
