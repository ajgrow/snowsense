package in2000.prosjekt.in2000project.data.model

import kotlinx.serialization.Serializable

data class GeoJson(
    val features: List<Feature> = emptyList(),
    val exceededTransferLimit: Boolean? = null
)


@Serializable
data class Feature(
    val properties: Properties? = null
)

@Serializable
data class Properties(
    val objekttype: String? = null,
    val skredtype: String? = null
)
