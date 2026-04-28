package in2000.prosjekt.in2000project.ui.reusables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.DangerLevel

@Composable
fun DangerLevelCard(dangerLevel: DangerLevel, backgroundColor: Color) {

    Surface(
        color = backgroundColor,
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Black)

    ) {
        when (dangerLevel) {
            DangerLevel.LOW -> Text(stringResource(R.string.danger_level_1_title), modifier = Modifier.padding(8.dp))
            DangerLevel.MODERATE -> Text(stringResource(R.string.danger_level_2_title), modifier = Modifier.padding(8.dp))
            DangerLevel.CONSIDERABLE -> Text(stringResource(R.string.danger_level_3_title), modifier = Modifier.padding(8.dp))
            DangerLevel.HIGH -> Text(stringResource(R.string.danger_level_4_title), modifier = Modifier.padding(8.dp))
            DangerLevel.VERY_HIGH -> Text(stringResource(R.string.danger_level_5_title), modifier = Modifier.padding(8.dp))
        }

    }
}

@Composable
fun DangerLevelDescriptionCard(dangerLevel: DangerLevel) {

    Surface(
        modifier = Modifier.padding(8.dp),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(12.dp)
    ) {
        when (dangerLevel) {
            DangerLevel.LOW -> Text(stringResource(R.string.danger_level_1_description), modifier = Modifier.padding(8.dp))
            DangerLevel.MODERATE -> Text(stringResource(R.string.danger_level_2_description), modifier = Modifier.padding(8.dp))
            DangerLevel.CONSIDERABLE -> Text(stringResource(R.string.danger_level_3_description), modifier = Modifier.padding(8.dp))
            DangerLevel.HIGH -> Text(stringResource(R.string.danger_level_4_description), modifier = Modifier.padding(8.dp))
            DangerLevel.VERY_HIGH -> Text(stringResource(R.string.danger_level_5_description), modifier = Modifier.padding(8.dp))
        }
    }
}


@Preview
@Composable
fun PreviewDangerLevelCard() {
    DangerLevelCard(DangerLevel.VERY_HIGH, backgroundColor = Color.Red)
}

@Preview
@Composable
fun PreviewDangerLevelDescriptionCard() {
    DangerLevelDescriptionCard(DangerLevel.VERY_HIGH)
}