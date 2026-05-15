package in2000.prosjekt.in2000project

import in2000.prosjekt.in2000project.MapboxAccessToken.value


/**
 * Singleton holder for the Mapbox access token.
 *
 * @property value The access token.
 */
object MapboxAccessToken {
    private const val MISSING_MSG = "MAPBOX_ACCESS_TOKEN missing. Add it to local.properties (see local.defaults.properties)"

    val value: String
        get() = BuildConfig.MAPBOX_ACCESS_TOKEN.also {
            require(it.isNotBlank()) { MISSING_MSG }
        }
}