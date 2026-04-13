package in2000.prosjekt.in2000project.ui.map

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
class MapViewModel: ViewModel() {
    var coordinates by mutableStateOf("65.0,10.0")
    fun updateLocation(coordinates: String) {
        this.coordinates = coordinates
    }
}