package in2000.prosjekt.in2000project.ui.information

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import in2000.prosjekt.in2000project.R
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DangerLevel(
    val level: String,
    val description: String
)

class InfoViewModel(application: Application): AndroidViewModel(application){

}