package in2000.prosjekt.in2000project.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Location (
    val name: String,
    val north: Double,
    val east: Double
)