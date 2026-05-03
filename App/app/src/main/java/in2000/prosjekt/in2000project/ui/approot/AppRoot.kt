package in2000.prosjekt.in2000project.ui.approot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import in2000.prosjekt.in2000project.navigation.AppNavigation

@Composable
fun AppRoot(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        AppNavigation()
    }
}