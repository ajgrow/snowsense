package in2000.prosjekt.in2000project.data.datasource.dto

import kotlinx.serialization.Serializable

@Serializable
data class FrostV0ObservationResponse (
    val currentItemCount: Int,
    val totalItemCount: Int,
    val data: List<FrostV0ObservationsAtRefTime>
)

@Serializable
data class FrostV0ObservationsAtRefTime (
    val sourceId: String,
    val referenceTime: String,
    val observations: List<FrostV0Observation>
)

@Serializable
data class FrostV0Observation (
    val elementId: String,
    val value: Double,
    val unit: String,
    val timeOffset: String,
    val timeResolution: String,
)