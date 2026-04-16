package in2000.prosjekt.in2000project.data.datasource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FrostV0SourceResponse (
    val data: List<FrostV0Source>
)

@Serializable
data class FrostV0Source (
    val id: String,
    val name: String,
    val shortName: String,
    val geometry: FrostV0Point,
    val distance: Double?,
    val masl: Double?,
    val validFrom: String?,
    val county: String?,
    val municipality: String?,
)

@Serializable
data class FrostV0Point (
    @SerialName("@type") val type: String,
    val coordinates: List<Double>
)