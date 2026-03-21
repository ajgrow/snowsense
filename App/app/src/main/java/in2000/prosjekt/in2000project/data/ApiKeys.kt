package in2000.prosjekt.in2000project.data

import in2000.prosjekt.in2000project.BuildConfig

object ApiKeys {
    val frostClientId: String
        get() = BuildConfig.FROST_CLIENT_ID.also {
            require(it.isNotBlank()) {
                "FROST_CLIENT_ID missing. Add it to local.properties (see local.defaults.properties)"
            }
        }

    val mapboxApiKey: String
        get() = BuildConfig.MAPBOX_ACCESS_TOKEN.also {
            require(it.isNotBlank()) {
                "MAPBOX_API_KEY missing. Add it to local.properties (see local.defaults.properties)"
            }
        }
}