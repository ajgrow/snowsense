package in2000.prosjekt.in2000project.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.data.SkiArea
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    // Hardcoded sample data for the MVP, however, when Frost API is ready this should move into the ViewModel
    val skiArea = remember {
        SkiArea(
            id = "1",
            name = "Lyngen Touring Zone",
            averageSnowfall = "Placeholder: 210 cm seasonal average",
            averageRainfall = "Placeholder: 95 mm monthly average",
            averageWind = "Placeholder: 7.8 m/s average wind speed",
            averageTemperature = "Placeholder: -3.2°C winter average",
            guideNotes = "Placeholder: Popular ski touring area with exposed alpine terrain"
        )
    }

    //Controls whether the bottom sheet is visible or not by keeping track of if the user has clicked the map
    //if this is set to null then the ModalBottomSheet is not open
    var selectedArea by remember { mutableStateOf<SkiArea?>(null) }

    //this creates a state object or "controller" that stores the UI for the bottom sheet
    //when skipPartiallyExpanded = true, this means that the sheet opens fully instead of being partially expanded
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Scaffold(
        topBar = {
            TopBar(modifier = Modifier, title = "MapScreen")
        },
        bottomBar = {
            BottomBar(modifier, navController)
        }
    ) { paddingValues ->

        // Main screen content
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
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Instructions
            Text(
                text = "Select an area to view historical climate details.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // FakeMap is just a placeholder for now. Once we have mapbox API, we can change this.
            FakeMap(
                onMapClick = {
                    selectedArea = skiArea
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    // Let lets you use a value only if it exists, but if it's null then nothing happens
    //This is for the bottom sheet - if there is a selectedArea, then it will open the bottom sheet
    //otherwise it will not open the bottom sheet
    selectedArea?.let { area ->
        ModalBottomSheet(
            onDismissRequest = { selectedArea = null },
            sheetState = sheetState
        ) {
            SkiAreaBottomSheetContent(
                // We pass the selected area (from the lambda) into the bottom sheet
                area = area,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
fun FakeMap(
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // This is the blue area on the MapScreen that is meant for where the map from MapBox will eventually go.
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFDCEEFF),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onMapClick() }
    ) {
        // "Map Placeholder" title inside the map area
        Text(
            text = "Map Placeholder",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        // Instructions inside the map area
        Text(
            text = "Tap a location",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 44.dp)
        )
    }
}


@Composable
fun SkiAreaBottomSheetContent(
    area: SkiArea,
    modifier: Modifier = Modifier
) {
    // This is the content inside the bottom sheet.
    // It shows the selected ski area's details.
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
            ClimateRow("Average snowfall", area.averageSnowfall)
            ClimateRow("Average rainfall", area.averageRainfall)
            ClimateRow("Average wind", area.averageWind)
            ClimateRow("Average temperature", area.averageTemperature)
            ClimateRow("Guide notes", area.guideNotes)
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
fun MapScreenPreview() {
    MapScreen(navController = rememberNavController())
}

@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
    val sampleArea = SkiArea(
        id = "1",
        name = "Lyngen Touring Zone",
        averageSnowfall = "210 cm seasonal average",
        averageRainfall = "95 mm monthly average",
        averageWind = "7.8 m/s average wind speed",
        averageTemperature = "-3.2°C winter average",
        guideNotes = "Popular ski touring area with exposed alpine terrain"
    )

    SkiAreaBottomSheetContent(
        area = sampleArea
    )
}