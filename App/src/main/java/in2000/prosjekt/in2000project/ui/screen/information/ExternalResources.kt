package in2000.prosjekt.in2000project.ui.screen.information

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import in2000.prosjekt.in2000project.R

/**
 * External resources data class.
 *
 * @param title The title of the external resources.
 * @param description The description of the external resources.
 * @param url The URL of the external resources.
 */
data class ExternalResource(
    val title: String,
    val description: String,
    val url: String
)


/**
 * External resources section for the app.
 */
@Composable
fun ExternalResources(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    val resources = listOf(
        ExternalResource(
            stringResource(R.string.varsom_title),
            stringResource(R.string.varsom_descr),
            stringResource(R.string.varsom_url)
        ),
        ExternalResource(
            stringResource(R.string.regobs_title),
            stringResource(R.string.regobs_desc),
            stringResource(R.string.regobs_url)
        ),
        ExternalResource(
            stringResource(R.string.nve_title),
            stringResource(R.string.nve_desc),
            stringResource(R.string.nve_url)
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        resources.forEach { resource ->
            ExternalResourceCard(resource = resource) {
                uriHandler.openUri(resource.url)
            }
        }
    }
}

@Composable
fun ExternalResourceCard(
    resource: ExternalResource,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 5.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = Bold
                )

                Text(
                    text = resource.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = stringResource(R.string.open_resource, resource.title)
            )
        }
    }
}
