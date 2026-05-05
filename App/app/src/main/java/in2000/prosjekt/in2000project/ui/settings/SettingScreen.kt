package in2000.prosjekt.in2000project.ui.settings

import androidx.appcompat.widget.DialogTitle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import in2000.prosjekt.in2000project.R
import androidx.compose.material.icons.filled.*
import in2000.prosjekt.in2000project.ui.reusables.TopBar


@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        settingsViewModel.refreshLanguageMode()
    }

    val themeMode = settingsViewModel.themeMode
    val textScale = settingsViewModel.textScale
    val languageMode = settingsViewModel.languageMode

    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {

            // Top bar

            TopBar(
                title = stringResource(R.string.settings_title),
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // appearance/theme

                Column {
                    Text(
                        text = stringResource(R.string.settings_appearance_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            ThemeSettingRow(
                                icon = Icons.Default.PhoneAndroid,
                                label = stringResource(R.string.settings_theme_system),
                                selected = themeMode == ThemeMode.SYSTEM,
                                onClick = { settingsViewModel.setTheme(ThemeMode.SYSTEM) }
                            )
                            SettingsDivider()
                            ThemeSettingRow(
                                icon = Icons.Default.LightMode,
                                label = stringResource(R.string.settings_theme_light),
                                selected = themeMode == ThemeMode.LIGHT,
                                onClick = { settingsViewModel.setTheme(ThemeMode.LIGHT) }
                            )
                            SettingsDivider()
                            ThemeSettingRow(
                                icon = Icons.Default.DarkMode,
                                label = stringResource(R.string.settings_theme_dark),
                                selected = themeMode == ThemeMode.DARK,
                                onClick = { settingsViewModel.setTheme(ThemeMode.DARK) }
                            )
                        }
                    }
                }

                // Text resizing
                Column {
                    Text(
                        text = stringResource(
                            R.string.settings_text_size_title_formatted,
                            (textScale * 100).toInt()
                        ).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TextFields,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Slider(
                                    value = textScale,
                                    onValueChange = { settingsViewModel.updateTextScale(it) },
                                    valueRange = 0.5f..2.0f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                IconButton(
                                    onClick = { settingsViewModel.resetTextScale() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = stringResource(R.string.settings_reset_default),
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.preview_text_size),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize * textScale
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // language / språk
                Column {
                    Text(
                        text = stringResource(R.string.settings_language_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (languageMode == AppLanguageMode.ENGLISH) "English"
                                    else "Norsk",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            LanguageControl(
                                selectedLanguage = languageMode,
                                onSelect = { settingsViewModel.setLanguage(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun LanguageControl(
    selectedLanguage: AppLanguageMode,
    onSelect: (AppLanguageMode) -> Unit
){

    val options = listOf(
        AppLanguageMode.ENGLISH to "English",
        AppLanguageMode.NORWEGIAN to "Norsk"
    )


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        options.forEach { (mode, label) ->
            val selected = selectedLanguage == mode
            val bgColor = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
            val textColor = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(bgColor)
                    .clickable {onSelect(mode)}
                    .padding(vertical = 12.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold
                                else FontWeight.Normal,
                    color = textColor
                )
            }
        }
    }
}





@Composable
private fun ThemeSettingRow(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(20.dp),
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
        )

        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsDivider(){
    HorizontalDivider(
        modifier = Modifier
            .padding(start = 52.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
