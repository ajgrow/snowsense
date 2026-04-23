package in2000.prosjekt.in2000project.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import in2000.prosjekt.in2000project.data.model.SkiArea
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.TopBar
import in2000.prosjekt.in2000project.ui.settings.SettingsViewModel

private val previewSkiArea = SkiArea(
    id = "1",
    name = "Galdhøpiggen Touring Zone",
    averageSnowfall = "210 cm seasonal average",
    averagePrecipitation = "95 mm monthly average",
    dailyMeanTemperature = "-3.2°C winter average"
)


//for use when the VM is ready
@Composable
fun MapScreenState(
    navController: NavController,
    viewModel: MapViewModel = viewModel(),
    nveViewModel: NveViewModel = viewModel(),
    coordinates: String = "65.0,10.0",
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val sheetState by viewModel.sheetState.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    MapScreen(
        navController = navController,
        mapUIState = uiState,
        nveViewModel = nveViewModel,
        coordinates = coordinates,
        onRetry = { viewModel.getMapData() },
        onOverlayLoaded = {viewModel.onOverlayLoaded()},
        onOverlayError = {viewModel.onOverlayError()},
        settingsViewModel = settingsViewModel,
        sheetState = sheetState,
        onOpenSheet = { viewModel.openSheet()},
        onCloseSheet = { viewModel.closeSheet()},
        onChangeSheet = { viewModel.changeSheetView()}
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    mapUIState: MapUIState,
    nveViewModel: NveViewModel,
    coordinates: String,
    onRetry: () -> Unit,
    onOverlayLoaded: () -> Unit,
    onOverlayError: () -> Unit,
    settingsViewModel: SettingsViewModel,
    sheetState: MapUISheetState,
    onOpenSheet: () -> Unit,
    onCloseSheet: () -> Unit,
    onChangeSheet: () -> Unit
) {

    // Hardcoded sample data for the MVP, however, when Frost API is ready this should move into the ViewModel
    val skiArea = remember { previewSkiArea }

    val dangerCheckUiState by nveViewModel.dangerCheckUiState.collectAsState()
    //Controls whether the bottom sheet is visible or not by keeping track of if the user has clicked the map
    //if this is set to null then the ModalBottomSheet is not open

    val avalancheStatus = when {
        dangerCheckUiState.isDanger == true -> "High"
        dangerCheckUiState.isDanger == false -> "Low"
        else -> "Haven't checked"
    }
    Scaffold(
        topBar = {
            TopBar(modifier = Modifier, title = "MapScreen", navController = navController)
        },
        bottomBar = {
            BottomBar(modifier, navController)
        }
    ) { paddingValues ->

        //Main screen content
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Makes sure the content is not hidden behind the top app bar or bottom app bar.
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Map",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Instructions
            Text(
                text = "Select an area to view historical climate details.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )




            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                //Map added here
                //onMapClick is passed to the Map
                MapboxView(
                    onMapClick = { point ->
                        onOpenSheet()
                        nveViewModel.avalancheChecker(point.latitude(), point.longitude())
                    },
                    onOverlayError = onOverlayError,
                    onOverlayLoaded = onOverlayLoaded,
                    modifier = Modifier
                        .fillMaxSize(),
                    coordinates = coordinates,
                    settingsViewModel = settingsViewModel
                )

                when (mapUIState) {
                    is MapUIState.Loading -> {
                        OverlayStatusCard(
                            message = "Loading avalanche overlay..."
                        )
                    }

                    is MapUIState.Error -> {
                        OverlayRetryCard(
                            message = "Could not load avalanche overlay.",
                            onRetry = onRetry
                        )
                    }

                    is MapUIState.Success -> Unit
                }
            }
        }
    }

    //This is for the bottom sheet - if isSheetOpen is set to true, then it will open otherwise, it stays closed
    if (sheetState.isSheetOpen){
        ModalBottomSheet(
            onDismissRequest = onCloseSheet,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            dragHandle = null
        ) {
            SkiAreaBottomSheetContent(
                area = skiArea,
                avalancheStatus = avalancheStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                sheetView = sheetState.sheetView,
                onChangeSheet = onChangeSheet
            )
        }
    }
}

@Composable
fun SkiAreaBottomSheetContent(
    area: SkiArea,
    avalancheStatus: String,
    modifier: Modifier = Modifier,
    sheetView: SheetView,
    onChangeSheet: () -> Unit,

) {
    // This is the content inside the bottom sheet.
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {onChangeSheet() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ){
                if (sheetView == SheetView.RISK){
                    Text("Show Trends")
                } else {
                    Text("Shoe Risk")
                }
            }

            when (sheetView){
                SheetView.RISK -> {
                    // Area name
                    Text(
                        text = area.name,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Description of the content
                    Text(
                        text = "Historical Climate Summary",
                        style = MaterialTheme.typography.titleMedium
                    )

                    HorizontalDivider()

                    // Placeholder rows for climate information
                    ClimateRow(label = "Avalanche danger", value = avalancheStatus)
                    ClimateRow("Average snowfall", area.averageSnowfall)
                    ClimateRow("Average precipitation", area.averagePrecipitation)
                    ClimateRow("Average temperature", area.dailyMeanTemperature)
                }

                SheetView.TRENDS ->{
                    val pagerState = rememberPagerState(pageCount =  {3 })
                    Text(
                        text = area.name,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "Climate Trends",
                        style = MaterialTheme.typography.titleMedium
                    )

                    HorizontalDivider()

                    val temperatureData = listOf(
                        2018 to -2f,
                        2019 to -1f,
                        2020 to 0f,
                        2021 to -3f,
                        2022 to 3f
                    )

                    val percipitationData = listOf(
                        2018 to 80f,
                        2019 to 95f,
                        2020 to 100f,
                        2021 to 90f,
                        2022 to 110f
                    )

                    val snowfallData = listOf(
                        2018 to 200f,
                        2019 to 220f,
                        2020 to 210f,
                        2021 to 230f,
                        2022 to 240f
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {page ->
                        val (title, data) = when (page){
                            0 -> "Temperature (°C)" to temperatureData
                            1 -> "Percipitation (mm)" to percipitationData
                            else -> "Snowfall (cm)" to snowfallData
                        }

                        val entries = remember(data) {
                            data.map { (year, value) ->
                                entryOf(year.toFloat(), value)
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Chart(
                                chart = lineChart(),
                                model = entryModelOf(entries),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(3){ index ->
                            val selected = pagerState.currentPage == index

                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .height(10.dp)
                                    .fillMaxWidth(0.02f)
                                    .background(color =
                                        if(selected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline
                                    )
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun ClimateRow(
    label: String,
    value: String
) {
    // This creates the rows of information within the bottom sheet i.e. Average snowfall, Average rainfall etc.
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview
@Composable
fun MapScreenErrorPreview() {
    MapScreen(
        navController = rememberNavController(),
        mapUIState = MapUIState.Error,
        nveViewModel = viewModel(),
        coordinates = "65.0,10.0",
        onRetry = {},
        onOverlayLoaded = {},
        onOverlayError = {},
        settingsViewModel = viewModel(),
        sheetState = MapUISheetState(
            isSheetOpen = true,
            sheetView = SheetView.RISK
        ),
        onOpenSheet = {},
        onCloseSheet = {},
        onChangeSheet = {}
    )
}


@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
    SkiAreaBottomSheetContent(
        area = previewSkiArea,
        avalancheStatus = "High",
        sheetView = SheetView.RISK,
        onChangeSheet = {}
    )
}