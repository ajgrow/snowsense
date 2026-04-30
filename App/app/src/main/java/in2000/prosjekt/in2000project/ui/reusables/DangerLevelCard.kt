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
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel

/**
 * REFACTORING NOTE:
 * These components now accept [DangerLevelUiModel] instead of the domain [DangerLevel] enum.
 * This follows Clean Architecture by ensuring that the UI layer only deals with
 * presentation-ready data. The logic for choosing colors and strings is moved
 * out of the Composable and into the ViewModel/Mapper.
 */

@Composable
fun DangerLevelCard(
    modifier: Modifier = Modifier,
    uiModel: DangerLevelUiModel,
    )
{
    Surface(
        color = uiModel.getColor(),
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Black)

    ) {
        Text(
            stringResource(uiModel.titleRes),
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun DangerLevelDescriptionCard(
    modifier: Modifier = Modifier,
    uiModel: DangerLevelUiModel) {

    Surface(
        modifier = modifier.padding(8.dp),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(12.dp)
    ) {

        Text(
            stringResource(uiModel.descriptionRes),
            modifier = Modifier.padding(8.dp)
        )
    }
}


@Preview
@Composable
fun PreviewDangerLevelCard() {
    DangerLevelCard(uiModel = DangerLevel.VERY_HIGH.toUiModel())
}

@Preview
@Composable
fun PreviewDangerLevelDescriptionCard() {
    DangerLevelDescriptionCard(uiModel = DangerLevel.VERY_HIGH.toUiModel())
}
