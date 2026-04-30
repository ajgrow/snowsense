package in2000.prosjekt.in2000project.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import in2000.prosjekt.in2000project.R

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        settingsViewModel.refreshLanguageMode()
    }

    val themeMode = settingsViewModel.themeMode
    val textScale = settingsViewModel.textScale
    val languageMode = settingsViewModel.languageMode

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }

                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button_desc)
                    )
                }
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSection(
                title = stringResource(R.string.appearance_title),
            ) {
                SettingsRadioOption(
                    title = stringResource(R.string.theme_system),
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { settingsViewModel.setTheme(ThemeMode.SYSTEM) },
                )

                SettingsRadioOption(
                    title = stringResource(R.string.theme_light),
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { settingsViewModel.setTheme(ThemeMode.LIGHT) },
                )

                SettingsRadioOption(
                    title = stringResource(R.string.theme_dark),
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { settingsViewModel.setTheme(ThemeMode.DARK) },
                )
            }

            SettingsSection(
                title = stringResource(R.string.text_size_title),
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Text size: ${(textScale * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Slider(
                        value = textScale,
                        onValueChange = { settingsViewModel.updateTextScale(it) },
                        valueRange = 0.5f..2.0f
                    )

                    Button(
                        onClick = { settingsViewModel.resetTextScale() },
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Text("Reset to default")
                    }
                }
            }

            SettingsSection(
                title = stringResource(R.string.language_title),
            ) {
                SettingsRadioOption(
                    title = "English",
                    selected = languageMode == AppLanguageMode.ENGLISH,
                    onClick = {
                        settingsViewModel.setLanguage(AppLanguageMode.ENGLISH)
                    }
                )

                SettingsRadioOption(
                    title = "Norsk",
                    selected = languageMode == AppLanguageMode.NORWEGIAN,
                    onClick = {
                        settingsViewModel.setLanguage(AppLanguageMode.NORWEGIAN)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.padding(start = 24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(
                top = 16.dp,
                bottom = 8.dp,
            ),
        )
        content()
    }
}

@Composable
fun SettingsRadioOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled
        )
        Text(
            text = title,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
