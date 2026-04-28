package in2000.prosjekt.in2000project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.LocaleListCompat
import com.mapbox.common.MapboxOptions
import in2000.prosjekt.in2000project.util.ApiKeys
import in2000.prosjekt.in2000project.navigation.AppNavigation
import in2000.prosjekt.in2000project.ui.theme.In2000projectTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Must be set before calling MapBox composable.
        MapboxOptions.accessToken = ApiKeys.mapboxAccessToken


        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        val savedLanguage = prefs.getString("language_mode", "ENGLISH")

        val locale = when (savedLanguage) {
            "NORWEGIAN" -> Locale.forLanguageTag("nb")
            else -> Locale.forLanguageTag("en")
        }

        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)


        setContent {
            In2000projectTheme {
                AppNavigation()
            }
        }
    }
}

