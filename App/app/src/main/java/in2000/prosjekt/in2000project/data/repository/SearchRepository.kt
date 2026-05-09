package in2000.prosjekt.in2000project.data.repository

import android.content.Context
import in2000.prosjekt.in2000project.domain.model.NamedLocation
import kotlinx.serialization.json.Json

class SearchRepository(context: Context) {
    private val prefs = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)

    fun saveFavorites(list: List<NamedLocation>) =
        prefs.edit().putString("favorites", Json.encodeToString(list)).apply()

    fun loadFavorites(): List<NamedLocation> {
        val json = prefs.getString("favorites", null) ?: return emptyList()
        return Json.decodeFromString(json)
    }

    fun saveRecentSearches(list: List<NamedLocation>) =
        prefs.edit().putString("recent_searches", Json.encodeToString(list)).apply()

    fun loadRecentSearches(): List<NamedLocation> {
        val json = prefs.getString("recent_searches", null) ?: return emptyList()
        return Json.decodeFromString(json)
    }
}