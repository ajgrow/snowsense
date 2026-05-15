package in2000.prosjekt.in2000project.data.datasource.dto

import kotlinx.serialization.Serializable

@Serializable
data class FrostV0ObservationResponseDto(
    val currentItemCount: Int,
    val totalItemCount: Int,
    val data: List<FrostV0ObservationsAtRefTimeDto>
)

@Serializable
data class FrostV0ObservationsAtRefTimeDto(
    val sourceId: String,
    val referenceTime: String,
    val observations: List<FrostV0ObservationDto>
)

@Serializable
data class FrostV0ObservationDto(
    val elementId: String,
    val value: Double,
    val unit: String,
    val timeOffset: String,
    val timeResolution: String,
)