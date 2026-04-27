package in2000.prosjekt.in2000project.ui.settings

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import in2000.prosjekt.in2000project.data.datastore.dataStore
import io.ktor.http.ContentType
import java.util.Locale

class SettingsViewModel(application: Application)
    : AndroidViewModel(application)
{
    private val prefs = application
        .getSharedPreferences("settings", Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(
        ThemeMode.valueOf(
            prefs.getString(
                "theme_mode", ThemeMode.SYSTEM.name)!!
        )
    )

    var textSizeMode by mutableStateOf(
        TextSizeMode.valueOf(
            prefs.getString(
                "text_size_mode", TextSizeMode.MEDIUM.name)!!
        )
    )

    var languageMode by mutableStateOf(
        AppCompatDelegate
            .getApplicationLocales()
            .toLanguageTags()
            .split(",")
            .firstOrNull()
            ?.let { AppLanguageMode.fromTag(it) }
            ?: AppLanguageMode.ENGLISH
    )

    fun setTheme(mode: ThemeMode){
        themeMode = mode
        prefs.edit().putString(
            "theme_mode", mode.name).apply()
    }

    fun setTextSize(mode: TextSizeMode){
        textSizeMode = mode
        prefs.edit().putString(
            "text_size_mode", mode.name).apply()
    }

    fun setLanguage(mode: AppLanguageMode) {
        languageMode = mode
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(mode.tag)
        )
    }

}