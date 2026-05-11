package in2000.prosjekt.in2000project.data.datasource.dto

import kotlinx.serialization.Serializable


@Serializable
data class GeoJsonResponseDto(
    val features: List<FeatureDto>?,
    val type: String?
)

@Serializable
data class FeatureDto(
    val geometry: GeometryDto?,
    val properties: PropertiesDto?,
)
@Serializable
data class GeometryDto(
    val coordinates: List<List<List<Double>>>?,
    val type: String?
)

@Serializable
data class PropertiesDto(
    val objekttype: String?,
    val skredtype: Int?
)