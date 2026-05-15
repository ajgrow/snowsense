package in2000.prosjekt.in2000project.data.datasource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FrostV0SourceResponseDto(
    val data: List<FrostV0SourceDto>
)

@Serializable
data class FrostV0SourceDto(
    val id: String? = null,
    val name: String? = null,
    val shortName: String? = null,
    val geometry: FrostV0PointDto? = null,
    val distance: Double? = null,
    val masl: Double? = null,
    val validFrom: String? = null,
    val county: String? = null,
    val municipality: String? = null,
)

@Serializable
data class FrostV0PointDto(
    @SerialName("@type") val type: String,
    val coordinates: List<Double>
)