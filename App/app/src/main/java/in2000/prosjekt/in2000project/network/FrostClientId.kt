package in2000.prosjekt.in2000project.network

import in2000.prosjekt.in2000project.BuildConfig

object FrostClientId {
    private const val MISSING_MSG = "FROST_CLIENT_ID missing. Add it to local.properties (see local.defaults.properties)"

    val value: String
        get() = BuildConfig.FROST_CLIENT_ID.also {
            require(it.isNotBlank()) { MISSING_MSG }
        }
}