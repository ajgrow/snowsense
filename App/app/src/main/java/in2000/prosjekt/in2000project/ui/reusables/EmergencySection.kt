package in2000.prosjekt.in2000project.ui.reusables
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import in2000.prosjekt.in2000project.R
import androidx.compose.ui.Alignment
import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
fun EmergencySection(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, Color.Red)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = stringResource(R.string.emergency_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = Bold,
            )
            Text(
                text = stringResource(R.string.emergency_desc),
                style = MaterialTheme.typography.bodySmall,
            )
            EmergencyRow(
                serviceResource = R.string.police_rescue_service,
                serviceNumberResource = R.string.police_rescue_number
            )
            EmergencyRow(
                serviceResource = R.string.medical_service,
                serviceNumberResource = R.string.medical_number
            )
        }
    }
}

@Composable
fun EmergencyRow(
    modifier: Modifier = Modifier,
    serviceResource: Int,
    serviceNumberResource: Int
) {
    val context = LocalContext.current
    val number = stringResource(serviceNumberResource)

    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(serviceResource),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        Card(
            onClick = { showDialog = true },
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, Color.Red),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = number,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = Bold
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = stringResource(
                        R.string.call_dialog_title,
                        number
                    )
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false

                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = "tel:$number".toUri()
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },

            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text(stringResource(R.string.dialog_no))
                }
            }
        )
    }
}

@Preview
@Composable
fun PrevEmergencySection() {
    EmergencySection()
}
