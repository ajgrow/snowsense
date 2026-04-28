package in2000.prosjekt.in2000project.ui.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import in2000.prosjekt.in2000project.R

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        settingsViewModel.refreshLanguageMode()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        val themeMode = settingsViewModel.themeMode
        val textSizeMode = settingsViewModel.textSizeMode
        val languageMode = settingsViewModel.languageMode

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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

            item {
                Text(
                    text = stringResource(R.string.appearance_title),
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                ThemeOption(
                    title = stringResource(R.string.theme_system),
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { settingsViewModel.setTheme(ThemeMode.SYSTEM) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                ThemeOption(
                    title = stringResource(R.string.theme_light),
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { settingsViewModel.setTheme(ThemeMode.LIGHT) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                ThemeOption(
                    title = stringResource(R.string.theme_dark),
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { settingsViewModel.setTheme(ThemeMode.DARK) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }


            item {
                Text(
                    text = stringResource(R.string.text_size_title),
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                ThemeOption(
                    title = stringResource(R.string.text_small),
                    selected = textSizeMode == TextSizeMode.SMALL,
                    onClick = { settingsViewModel.setTextSize(TextSizeMode.SMALL) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                ThemeOption(
                    title = stringResource(R.string.text_medium),
                    selected = textSizeMode == TextSizeMode.MEDIUM,
                    onClick = { settingsViewModel.setTextSize(TextSizeMode.MEDIUM) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                ThemeOption(
                    title = stringResource(R.string.text_large),
                    selected = textSizeMode == TextSizeMode.LARGE,
                    onClick = { settingsViewModel.setTextSize(TextSizeMode.LARGE) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Text(
                    text = stringResource(R.string.language_title),
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                ThemeOption(
                    title = "English",
                    selected = languageMode == AppLanguageMode.ENGLISH,
                    onClick = {
                        settingsViewModel.setLanguage(AppLanguageMode.ENGLISH)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                ThemeOption(
                    title = "Norsk",
                    selected = languageMode == AppLanguageMode.NORWEGIAN,
                    oonClick = {
                        settingsViewModel.setLanguage(AppLanguageMode.NORWEGIAN)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}



@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
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