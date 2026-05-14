package in2000.prosjekt.in2000project.data.repository

import android.content.Context
import in2000.prosjekt.in2000project.domain.model.NamedLocation
import in2000.prosjekt.in2000project.domain.repository.SearchRepository
import kotlinx.serialization.json.Json
import androidx.core.content.edit

class SharedPreferencesSearchRepository(
    context: Context
) : SearchRepository {
    private val prefs = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)

    override fun saveFavorites(list: List<NamedLocation>) =
        prefs.edit { putString("favorites", Json.encodeToString(list)) }

    override fun loadFavorites(): List<NamedLocation> {
        val json = prefs.getString("favorites", null) ?: return emptyList()
        return Json.decodeFromString(json)
    }

    override fun saveRecentSearches(list: List<NamedLocation>) =
        prefs.edit { putString("recent_searches", Json.encodeToString(list)) }

    override fun loadRecentSearches(): List<NamedLocation> {
        val json = prefs.getString("recent_searches", null) ?: return emptyList()
        return Json.decodeFromString(json)
    }
}