package in2000.prosjekt.in2000project.data.model

import kotlinx.serialization.Serializable


@Serializable
data class GeoJson(
    val exceededTransferLimit: Boolean?,
    val features: List<Feature>?,
    val properties: PropertiesX?,
    val type: String?
)

@Serializable
data class Feature(
    val geometry: Geometry?,
    val properties: Properties?,
    val type: String?
)

@Serializable
data class PropertiesX(
    val exceededTransferLimit: Boolean?
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