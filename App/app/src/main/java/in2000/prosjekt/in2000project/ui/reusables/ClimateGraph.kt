package in2000.prosjekt.in2000project.ui.reusables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import in2000.prosjekt.in2000project.R
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import in2000.prosjekt.in2000project.data.model.LocationClimateDataPoint
import java.time.MonthDay

@Composable
fun ClimateGraph(
    dataPoints: List<LocationClimateDataPoint>,
    startDate: MonthDay,
    endDate: MonthDay,
    title: String
) {
    val entries = dataPoints.map { point ->
        entryOf(point.year.toFloat(), point.avgValue.toFloat())
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = "$title (${startDate.display()} - ${endDate.display()})")

        if (!entries.isEmpty()) {
            Chart(
                chart = lineChart(),
                model = entryModelOf(entries),
                startAxis = rememberStartAxis(
                    label = textComponent(
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                ),
                bottomAxis = rememberBottomAxis(
                    label = textComponent(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_graph_data), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}