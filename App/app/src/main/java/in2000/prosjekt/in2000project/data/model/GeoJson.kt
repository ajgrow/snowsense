package in2000.prosjekt.in2000project.data.model

import kotlinx.serialization.Serializable


@Serializable
data class GeoJson(
    val features: List<Feature>?,
    val type: String?
)

@Serializable
data class Feature(
    val geometry: Geometry?,
    val properties: Properties?,
)
@Serializable
data class Geometry(
    val coordinates: List<List<List<Double?>?>?>?,
    val type: String?
)

@Serializable
data class Properties(
    val objekttype: String?,
    val skredtype: Int?
)