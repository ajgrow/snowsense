package in2000.prosjekt.in2000project.data.datasource.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationResponse (
    val navn : List<LocationDto>
)

@Serializable
data class LocationDto (
    val representasjonspunkt: CoordinateDto,
    val skrivemåte: String
)

@Serializable
data class CoordinateDto (
    val nord: Double,
    val øst: Double
)