package in2000.prosjekt.in2000project.datastore.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.datastore by preferencesDataStore(name = "settings")

private val THEME_KEY = stringPreferencesKey("theme_mode")
private val TEXT_SIZE_KEY = stringPreferencesKey("text_size_mode")

