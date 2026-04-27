package in2000.prosjekt.in2000project.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import in2000.prosjekt.in2000project.ui.settings.TextSizeMode
import in2000.prosjekt.in2000project.ui.settings.ThemeMode
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    val darkTheme = when (themeMode){
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
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

    val view = LocalView.current

    if(!view.isInEditMode){
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = colorScheme.background.toArgb()

            val insetsController = WindowCompat.getInsetsController(window, view)

            insetsController.isAppearanceLightStatusBars = !darkTheme

            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = chosenTypography,
        content = content
    )
}