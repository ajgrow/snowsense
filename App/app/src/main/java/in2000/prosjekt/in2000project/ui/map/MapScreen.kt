package in2000.prosjekt.in2000project.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.TopBar

// This data class represents one ski touring area.
// Each area has a name and some placeholder climate info that we will use when we have frost
data class SkiArea(
    val id: String,
    val name: String,
    val averageSnowfall: String,
    val averageRainfall: String,
    val averageWind: String,
    val averageTemperature: String,
    val guideNotes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    // Hardcoded sample data for the MVP.
    // Later, this can come from your API instead.
    val skiAreas = remember {
        listOf(
            SkiArea(
                id = "1",
                name = "Lyngen Touring Zone",
                averageSnowfall = "Placeholder: 210 cm seasonal average",
                averageRainfall = "Placeholder: 95 mm monthly average",
                averageWind = "Placeholder: 7.8 m/s average wind speed",
                averageTemperature = "Placeholder: -3.2°C winter average",
                guideNotes = "Placeholder: Popular ski touring area with exposed alpine terrain"
            ),
            SkiArea(
                id = "2",
                name = "Tromsdalstinden Approach",
                averageSnowfall = "Placeholder: 185 cm seasonal average",
                averageRainfall = "Placeholder: 88 mm monthly average",
                averageWind = "Placeholder: 6.9 m/s average wind speed",
                averageTemperature = "Placeholder: -4.1°C winter average",
                guideNotes = "Placeholder: Add route-specific snow and terrain notes here"
            ),
            SkiArea(
                id = "3",
                name = "Hemsedal Touring Area",
                averageSnowfall = "Placeholder: 160 cm seasonal average",
                averageRainfall = "Placeholder: 76 mm monthly average",
                averageWind = "Placeholder: 5.4 m/s average wind speed",
                averageTemperature = "Placeholder: -2.0°C winter average",
                guideNotes = "Placeholder: Good beginner touring area with variable wind exposure"
            )
        )
    }

    // This keeps track of which ski area the user has tapped.
    // If it is null, no bottom sheet is shown.
    var selectedArea by remember { mutableStateOf<SkiArea?>(null) }

    // This controls the state of the bottom sheet.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Scaffold is the main screen layout.
    // It lets you place a top bar, bottom bar, and screen content.
    Scaffold(
        topBar = {
            // Replace this with your real top app bar composable.
            TopBar(modifier = Modifier, title = "TBD")
        },
        bottomBar = {
            // Replace this with your real bottom app bar composable.
            BottomBar()
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
            // Screen title
            Text(
                text = "Map",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Instructional text
            Text(
                text = "Select an area to view historical climate details.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // FakeMap is just a placeholder for now.
            // Later, this can be replaced with a real map.
            FakeMap(
                areas = skiAreas,
                onAreaClick = { area ->
                    selectedArea = area
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    // Show the bottom sheet only when the user has selected an area.
    if (selectedArea != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedArea = null },
            sheetState = sheetState
        ) {
            SkiAreaBottomSheetContent(
                area = selectedArea!!,
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
    areas: List<SkiArea>,
    onAreaClick: (SkiArea) -> Unit,
    modifier: Modifier = Modifier
) {
    // This is a fake map box for the MVP.
    // It is just a styled container with clickable markers.
    Box(
        modifier = modifier
            .background(
                color = Color(0xFFDCEEFF),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // "Map Placeholder" title inside the fake map
        Text(
            text = "Map Placeholder",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        // Instruction text inside the fake map
        Text(
            text = "Tap a location",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 44.dp)
        )

        // First fake marker
        FakeMapMarker(
            label = areas[0].name,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 32.dp, y = 120.dp),
            onClick = { onAreaClick(areas[0]) }
        )

        // Second fake marker
        FakeMapMarker(
            label = areas[1].name,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 40.dp, y = (-20).dp),
            onClick = { onAreaClick(areas[1]) }
        )

        // Third fake marker
        FakeMapMarker(
            label = areas[2].name,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-36).dp, y = (-90).dp),
            onClick = { onAreaClick(areas[2]) }
        )
    }
}

@Composable
fun FakeMapMarker(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // This composable acts like a fake map marker.
    // It includes a circular icon area and a label below it.
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular marker icon
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFF1565C0),
                    shape = CircleShape
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "📍",
                color = Color.White
            )
        }

        // Label shown under the marker
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            tonalElevation = 2.dp,
            modifier = Modifier.padding(top = 6.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
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

            // Section title
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
    // This creates one labeled row of information
    // inside the bottom sheet.
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
    MapScreen()
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