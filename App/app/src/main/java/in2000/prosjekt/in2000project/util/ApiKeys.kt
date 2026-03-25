package in2000.prosjekt.in2000project.util

import in2000.prosjekt.in2000project.BuildConfig

object ApiKeys {
    private const val SECRETS_LOCATION_MSG =
        "%s missing. Add it to local.properties (see local.defaults.properties)"

    val frostClientId: String
        get() = BuildConfig.FROST_CLIENT_ID.also {
            require(it.isNotBlank()) {
                SECRETS_LOCATION_MSG.format("FROST_CLIENT_ID")
            }
        }

    val mapboxAccessToken: String
        get() = BuildConfig.MAPBOX_ACCESS_TOKEN.also {
            require(it.isNotBlank()) {
                SECRETS_LOCATION_MSG.format("MAPBOX_ACCESS_TOKEN")
            }
        }
}