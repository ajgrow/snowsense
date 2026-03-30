package in2000.prosjekt.in2000project.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationResponse (
    val navn : List<Name>
)