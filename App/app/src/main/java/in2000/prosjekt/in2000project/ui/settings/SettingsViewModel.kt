package in2000.prosjekt.in2000project.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel(){

    private val _themeMode = mutableStateOf(ThemeMode.SYSTEM)
    val themeMode  = _themeMode

    private val _textSizeMode = mutableStateOf(TextSizeMode.MEDIUM)
    val textSizeMode = _textSizeMode


    fun setTheme(mode: ThemeMode){
        _themeMode.value = mode
    }

    fun setTextSize(mode: TextSizeMode){
        _textSizeMode.value = mode
    }
}