package in2000.prosjekt.in2000project.ui.information

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import in2000.prosjekt.in2000project.ui.reusables.BottomBar
import in2000.prosjekt.in2000project.ui.reusables.TopBar

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    Scaffold(
        topBar = {
            TopBar(modifier = Modifier, title = "InformationScreen")
        },
        bottomBar = {
            BottomBar(modifier, navController)
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "About the app",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "The Team",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "We are a group of incredibly talented, wonderfully creative, and pretty f***ing cool IN2000 students. We have been tasked with making the coooooooolest app you've ever seen and that's what we plan to do. This page is just FYI, but click back to Home or the Map for more :)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Avalanche Info",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Avalanches have become increasingly more common as Global Climate Change keeps on keeping on. So when you're out shreddin', you gots ta be careful. That's why we created this app! We hope you enjoy it.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Avalanche Danger Levels",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    DangerLevelsColumn()
                }
            }
        }
    }
}

@Composable
fun DangerLevelsColumn() {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DangerLevelItem(
            level = "1 - Low",
            description = "NICE! This means generally safe avalanche conditions. Natural and human-triggered avalanches are unlikely. GO! GO! GO!"
        )

        DangerLevelItem(
            level = "2 - Moderate",
            description = "Hmmm... maybe think twice before taking this on? Heightened avalanche conditions on some terrain features. Human-triggered avalanches are definitely possible."
        )

        DangerLevelItem(
            level = "3 - Considerable",
            description = "YIKES, POSSIBLE DEATH - Dangerous avalanche conditions. PLEASE DON'T GO! Human-triggered avalanches are likely and natural avalanches are possible."
        )

        DangerLevelItem(
            level = "4 - High",
            description = "DEATH IS LIKELY - DO NOT GO!! Very dangerous avalanche conditions. Human-triggered avalanches are very likely and natural avalanches are likely."
        )

        DangerLevelItem(
            level = "5 - Extreme",
            description = "CERTAIN DEATH!! At this point, it might be time to book a flight south. Avoid all avalanche terrain. Large natural avalanches are certain or nearly certain."
        )
    }
}
@Composable
fun DangerLevelItem(
    level: String,
    description: String
) {
    Column {
        Text(
            text = level,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall
        )
    }
}