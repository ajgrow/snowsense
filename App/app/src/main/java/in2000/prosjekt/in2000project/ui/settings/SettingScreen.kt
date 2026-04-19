package in2000.prosjekt.in2000project.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {navController.popBackStack()}
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back Button"
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Appearance",
                modifier = Modifier.padding(16.dp)
            )

            ThemeOption(
                title = "Use device setting",
                selected = settingsViewModel.themeMode.value == ThemeMode.SYSTEM,
                onClick = {settingsViewModel.setTheme(ThemeMode.SYSTEM)},
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            ThemeOption(
                title = "Light Mode",
                selected = settingsViewModel.themeMode.value == ThemeMode.LIGHT,
                onClick = {settingsViewModel.setTheme(ThemeMode.LIGHT)},
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            ThemeOption(
                title = "Dark Mode",
                selected = settingsViewModel.themeMode.value == ThemeMode.DARK,
                onClick = {settingsViewModel.setTheme(ThemeMode.DARK)},
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}



@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier
) {
    Row(
        modifier = Modifier
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