package in2000.prosjekt.in2000project.ui.reusables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.ui.model.DangerLevelUiModel
import in2000.prosjekt.in2000project.ui.model.toUiModel

/*
These components now accept DangerLevelUiModel instead of the DangerLevel enum.
The [uiModel] is nullable. If null, a neutral "Not checked" card is displayed.
 */

/*
  * highlightNumberOnly:  Show number as colored chip (DangerLevelItem)
  * or plain text (used in DangerLevelCard)
*/
@Composable
fun DangerLevelHeader(
    modifier: Modifier = Modifier,
    uiModel: DangerLevelUiModel,
    highlightNumberOnly: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (highlightNumberOnly) {
            Surface(
                color = uiModel.getColor(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(uiModel.numberRes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        } else {
            Text(
                text = stringResource(uiModel.numberRes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = stringResource(uiModel.titleRes),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = Bold
        )
    }
}

@Composable
fun DangerLevelItem(
    modifier: Modifier = Modifier,
    uiModel: DangerLevelUiModel
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, uiModel.getColor())
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DangerLevelHeader(uiModel = uiModel)

                Text(
                    text = if (expanded)
                        stringResource(R.string.hide_info)
                    else
                        stringResource(R.string.show_info),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            AnimatedVisibility(visible = expanded) {
                Text(
                    text = stringResource(uiModel.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun DangerLevelCard(
    modifier: Modifier = Modifier,
    uiModel: DangerLevelUiModel?,
) {
    val color = uiModel?.getColor()
        ?: MaterialTheme.colorScheme.surfaceVariant

    Surface(
        color = color,
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        if (uiModel == null) {
            Text(
                text = stringResource(R.string.not_checked),
                modifier = Modifier.padding(8.dp)
            )
        } else {
            DangerLevelHeader(
                uiModel = uiModel,
                highlightNumberOnly = false,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun DangerLevelDescriptionCard(
    modifier: Modifier = Modifier,
    uiModel: DangerLevelUiModel
) {
    Surface(
        modifier = modifier.padding(8.dp),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = stringResource(uiModel.descriptionRes),
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview
@Composable
fun PreviewDangerLevelHeader() {
    DangerLevelHeader(uiModel = DangerLevel.VERY_HIGH.toUiModel())
}

@Preview
@Composable
fun PreviewDangerLevelCard() {
    DangerLevelCard(uiModel = DangerLevel.VERY_HIGH.toUiModel())
}

@Preview
@Composable
fun PreviewDangerLevelCardNotChecked() {
    DangerLevelCard(uiModel = null)
}

@Preview
@Composable
fun PrevDangerLevelItem() {
    DangerLevelItem(uiModel = DangerLevel.VERY_HIGH.toUiModel())
}


@Preview
@Composable
fun PreviewDangerLevelDescriptionCard() {
    DangerLevelDescriptionCard(uiModel = DangerLevel.VERY_HIGH.toUiModel())
}