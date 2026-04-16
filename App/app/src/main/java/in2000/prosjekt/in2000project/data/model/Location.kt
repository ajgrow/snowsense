package in2000.prosjekt.in2000project.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Location (
    val representasjonspunkt: RepresentationPoint,
    val skrivemåte: String
)