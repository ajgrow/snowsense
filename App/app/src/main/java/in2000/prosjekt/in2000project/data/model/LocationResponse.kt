package in2000.prosjekt.in2000project.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationResponseDTO (
    val navn : List<LocationDTO>
)

@Serializable
data class LocationDTO (
    val representasjonspunkt: RepresentationPointDTO,
    val skrivemåte: String
)

@Serializable
data class RepresentationPointDTO (
    val nord: Double,
    val øst: Double
)