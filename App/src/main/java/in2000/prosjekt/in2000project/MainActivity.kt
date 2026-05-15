package in2000.prosjekt.in2000project

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.common.MapboxOptions
import in2000.prosjekt.in2000project.ui.approot.AppRoot
import in2000.prosjekt.in2000project.ui.theme.In2000projectTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Must be set before calling MapBox composable.
        MapboxOptions.accessToken = MapboxAccessToken.value

        setContent {
            In2000projectTheme {
                AppRoot()
            }
        }
    }
}