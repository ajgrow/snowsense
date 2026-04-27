package in2000.prosjekt.in2000project

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.common.MapboxOptions
import in2000.prosjekt.in2000project.util.ApiKeys
import in2000.prosjekt.in2000project.navigation.AppNavigation
import in2000.prosjekt.in2000project.ui.theme.In2000projectTheme
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Must be set before calling MapBox composable.
        MapboxOptions.accessToken = ApiKeys.mapboxAccessToken

        setContent {
            In2000projectTheme {
                AppNavigation()
            }
        }
    }
}

