package in2000.prosjekt.in2000project.ui.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.SkiArea
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.DatePicker
import in2000.prosjekt.in2000project.ui.reusables.TopBar
import java.time.MonthDay
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.component.textComponent
import in2000.prosjekt.in2000project.data.model.DangerLevel
import in2000.prosjekt.in2000project.ui.reusables.ClimateGraph
import in2000.prosjekt.in2000project.ui.reusables.DangerLevelCard
import in2000.prosjekt.in2000project.ui.reusables.DangerLevelDescriptionCard
import androidx.compose.material3.Surface

private val previewSkiArea = SkiArea(
    id = "1",
    name = "Galdhøpiggen",
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
) {
    val uiState by viewModel.uiState.collectAsState()

    MapScreen(
        navController = navController,
        mapUIState = uiState,
        nveViewModel = nveViewModel,
        coordinates = coordinates,
        onRetry = { viewModel.getMapData() },
        onOverlayLoaded = {viewModel.onOverlayLoaded()},
        onOverlayError = {viewModel.onOverlayError()},
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
) {

    // Hardcoded sample data for the MVP, however, when Frost API is ready this should move into the ViewModel
    val skiArea = remember { previewSkiArea }

    val dangerCheckUiState by nveViewModel.dangerCheckUiState.collectAsState()
    //Controls whether the bottom sheet is visible or not by keeping track of if the user has clicked the map
    //if this is set to null then the ModalBottomSheet is not open
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }

    var skipPartiallyExpanded by rememberSaveable {mutableStateOf(false)}

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)


    val avalancheStatus = when {
        dangerCheckUiState.isDanger == true -> "High"
        dangerCheckUiState.isDanger == false -> "Low"
        else -> "Haven't checked"
    }

    var startDate by rememberSaveable { mutableStateOf<MonthDay?>(null) }
    var endDate by rememberSaveable { mutableStateOf<MonthDay?>(null) }
    var selectedYear by rememberSaveable { mutableStateOf<Int?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                modifier = Modifier,
                title = stringResource(R.string.map_screen_title),
                navController = navController)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                //Map added here
                //onMapClick is passed to the Map
                MapboxView(
                    onMapClick = { point ->
                        isSheetOpen = true
                        nveViewModel.avalancheChecker(point.latitude(), point.longitude())
                    },
                    onOverlayError = onOverlayError,
                    onOverlayLoaded = onOverlayLoaded,
                    modifier = Modifier
                        .fillMaxSize(),
                    coordinates = coordinates,
                )

                when (mapUIState) {
                    is MapUIState.Loading -> {
                        OverlayStatusCard(
                            message = stringResource(R.string.overlay_loading)
                        )
                    }

                    is MapUIState.Error -> {
                        OverlayRetryCard(
                            message = stringResource(R.string.overlay_error),
                            onRetry = onRetry
                        )
                    }

                    is MapUIState.Success -> Unit
                }
            }
        }
    }

    //This is for the bottom sheet - if isSheetOpen is set to true, then it will open otherwise, it stays closed
    if (isSheetOpen){
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxHeight()
        ) {
            BottomSheetContent(
                area = skiArea,
                avalancheStatus = avalancheStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                startDate = startDate,
                endDate = endDate,
                selectedYear = selectedYear,
                onStartDateChange = { startDate = it },
                onEndDateChange = { endDate = it },
                onYearChange = { selectedYear = it }
            )
        }
    }
}

@Composable
fun BottomSheetContent(
    area: SkiArea,
    avalancheStatus: String,
    modifier: Modifier = Modifier,
    startDate: MonthDay?,
    endDate: MonthDay?,
    selectedYear: Int?,
    onStartDateChange: (MonthDay?) -> Unit,
    onEndDateChange: (MonthDay?) -> Unit,
    onYearChange: (Int?) -> Unit,
) {

    val climateRows = listOf(
        stringResource(R.string.label_avalanche_danger) to avalancheStatus,
        stringResource(R.string.label_average_snowfall) to "TODO",
        stringResource(R.string.label_average_precipitation) to "TODO",
        stringResource(R.string.label_average_temperature) to "TODO"
    )

    // This is the content inside the bottom sheet.
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Area name
                    Text(
                        text = area.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    DangerLevelCard(dangerLevel = DangerLevel.VERY_HIGH, backgroundColor = Color.Red)
                }
            }

            item {
                DangerLevelDescriptionCard(dangerLevel = DangerLevel.VERY_HIGH)
            }

            item {
                // Description of the content
                Text(
                    text = stringResource(R.string.historical_climate_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                DatePicker(
                    startDate = startDate,
                    endDate = endDate,
                    selectedYear = selectedYear,
                    onStartDateChange = onStartDateChange,
                    onEndDateChange = onEndDateChange,
                    onYearChange = onYearChange,
                )

                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = stringResource(R.string.date_description),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            item {
                HorizontalDivider()
            }

            items(climateRows) { row ->
                ClimateRow(
                    label = row.first,
                    value = row.second)
            }

            item {
                HorizontalDivider()
            }

            //THIS IS WHERE WE CAN PUT THE GRAPH COMPONENTS
            item {
                val currentYear = java.time.Year.now().value

                val yearRange = remember(selectedYear) {
                    selectedYear?.let { it..currentYear }
                }

                if (yearRange == null ||
                    startDate == null ||
                    endDate == null
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Please select a year range to view graph")
                    }
                } else {
                    ClimateGraph(yearRange, startDate, endDate)
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
    )
}


@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
    BottomSheetContent(
        area = previewSkiArea,
        avalancheStatus = "High",
        startDate = null,
        endDate = null,
        selectedYear = null,
        onStartDateChange = { },
        onEndDateChange = { },
        onYearChange = { },
    )
}