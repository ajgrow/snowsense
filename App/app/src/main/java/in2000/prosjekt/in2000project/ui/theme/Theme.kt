package in2000.prosjekt.in2000project.ui.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import in2000.prosjekt.in2000project.ui.settings.TextSizeMode
import in2000.prosjekt.in2000project.ui.settings.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)
@Composable
fun In2000projectTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    textSizeMode: TextSizeMode = TextSizeMode.MEDIUM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode){
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    // Refresh edge-to-edge when the theme changes so status bar icons update
    (LocalActivity.current as? ComponentActivity)?.let { activity ->
        DisposableEffect(isDarkTheme) {
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                    ) { isDarkTheme },
                navigationBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                    ) { isDarkTheme },
            )
            onDispose { }
        }
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val chosenTypography = when (textSizeMode){
        TextSizeMode.SMALL -> Typography.copy(
            bodyLarge = Typography.bodyLarge.copy(fontSize = Typography.bodyLarge.fontSize * 0.85f),
            titleLarge = Typography.titleLarge.copy(fontSize = Typography.titleLarge.fontSize * 0.85f)
        )

        TextSizeMode.MEDIUM -> Typography

        TextSizeMode.LARGE -> Typography.copy(
            bodyLarge = Typography.bodyLarge.copy(fontSize = Typography.bodyLarge.fontSize * 1.2f),
            titleLarge = Typography.titleLarge.copy(fontSize = Typography.titleLarge.fontSize * 1.2f)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = chosenTypography,
        content = content
    )
}