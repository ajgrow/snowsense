package in2000.prosjekt.in2000project.ui.screen.information

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import in2000.prosjekt.in2000project.R

/**
 * Safety advice section for the app.
 *
 * @param title The title of the section.
 * @param bullets The list of bullets to display.
 * @param showDivider Whether to show a divider between the section and the next section.
 */
@Composable
fun SafetyAdvice(
    modifier: Modifier = Modifier,
    title: String,
    bullets: List<String>,
    showDivider: Boolean = true
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val expandedText = stringResource(if (expanded) R.string.hide_info else R.string.show_info)
    val expandedDescription =
        stringResource(if (expanded) R.string.is_expanded else R.string.is_not_expanded)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { expanded = !expanded }
                .semantics {
                    stateDescription = expandedDescription
                }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = expandedText,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                bullets.forEach { bullet ->
                    Text(
                        text = "• $bullet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
fun SafetyAdviceSection(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 8.dp,
                    top = 10.dp,
                    end = 8.dp,
                    bottom = 6.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.safety_advice_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = Bold,
                    modifier = Modifier.semantics { heading() }
                )

                Text(
                    text = stringResource(R.string.safety_advice_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            ) {
                SafetyAdvice(
                    title = stringResource(R.string.before_you_go_title),
                    bullets = listOf(
                        stringResource(R.string.before_you_go_1),
                        stringResource(R.string.before_you_go_2),
                        stringResource(R.string.before_you_go_3),
                        stringResource(R.string.before_you_go_4)
                    )
                )

                SafetyAdvice(
                    title = stringResource(R.string.while_you_are_out_title),
                    bullets = listOf(
                        stringResource(R.string.while_you_are_out_1),
                        stringResource(R.string.while_you_are_out_2),
                        stringResource(R.string.while_you_are_out_3),
                        stringResource(R.string.while_you_are_out_4)
                    )
                )
                SafetyAdvice(
                    title = stringResource(R.string.if_something_goes_wrong_title),
                    bullets = listOf(
                        stringResource(R.string.if_something_goes_wrong_1),
                        stringResource(R.string.if_something_goes_wrong_2),
                        stringResource(R.string.if_something_goes_wrong_3),
                        stringResource(R.string.if_something_goes_wrong_4)
                    ),
                    showDivider = false
                )
            }
        }
    }
}
