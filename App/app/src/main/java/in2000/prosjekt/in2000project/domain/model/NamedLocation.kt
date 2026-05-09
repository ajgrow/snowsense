package in2000.prosjekt.in2000project.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NamedLocation (
    val name: String,
    val coordinate: Coordinate,
    val locationTypeNo: String = "",
    val locationTypeEn: String = ""
)