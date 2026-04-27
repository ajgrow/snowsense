package in2000.prosjekt.in2000project.ui.reusables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import java.time.MonthDay

@Composable
fun ClimateGraph(
    yearRange: IntRange,
    startDate: MonthDay,
    endDate: MonthDay,
) {
    val points = remember(yearRange, startDate, endDate){
        yearRange.map {year ->
            year.toFloat() to 50f //VALUE GOES HERE
        }
    }

    val entries = points.map {
        entryOf(it.first, it.second)
    }

    Text(
        text = "From $startDate to $endDate",
        style = MaterialTheme.typography.titleMedium
    )

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
}