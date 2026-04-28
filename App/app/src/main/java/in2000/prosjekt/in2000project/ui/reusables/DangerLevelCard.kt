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
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel

@Composable
fun DangerLevelCard(
    modifier: Modifier = Modifier,
    dangerLevel: DangerLevel,
    )
{

    Surface(
        color = dangerLevel.getColor(),
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Black)

    ) {
        Text(
            stringResource(dangerLevel.titleRes),
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun DangerLevelDescriptionCard(
    modifier: Modifier = Modifier,
    dangerLevel: DangerLevel) {

    Surface(
        modifier = modifier.padding(8.dp),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(12.dp)
    ) {

        Text(
            stringResource(dangerLevel.descriptionRes),
            modifier = Modifier.padding(8.dp)
        )
    }
}


@Preview
@Composable
fun PreviewDangerLevelCard() {
    DangerLevelCard(dangerLevel = DangerLevel.VERY_HIGH)
}

@Preview
@Composable
fun PreviewDangerLevelDescriptionCard() {
    DangerLevelDescriptionCard(dangerLevel = DangerLevel.VERY_HIGH)
}