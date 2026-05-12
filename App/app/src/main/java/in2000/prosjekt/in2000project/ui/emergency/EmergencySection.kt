package in2000.prosjekt.in2000project.ui.emergency

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import in2000.prosjekt.in2000project.R

@Composable
fun EmergencySection(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, color = Color(0xFF8B0000))
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = stringResource(R.string.call_icon),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(16.dp)
                    )

                    Text(
                        text = stringResource(R.string.emergency_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = Bold
                    )
                }

                Text(
                    text = stringResource(R.string.emergency_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            ) {
                EmergencyRow(
                    serviceResource = R.string.police_rescue_service,
                    serviceNumberResource = R.string.police_rescue_number,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                EmergencyRow(
                    serviceResource = R.string.medical_service,
                    serviceNumberResource = R.string.medical_number
                )
            }
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(serviceResource),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            modifier = Modifier.clickable { showDialog = true },
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.error,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = stringResource(R.string.call_icon),
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(14.dp)
                )

                Text(
                    text = number,
                    color = MaterialTheme.colorScheme.onError,
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