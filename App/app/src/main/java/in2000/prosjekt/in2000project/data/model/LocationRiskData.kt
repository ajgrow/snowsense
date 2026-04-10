package in2000.prosjekt.in2000project.data.model

import in2000.prosjekt.in2000project.ui.information.DangerLevel

data class LocationSlabAvalancheRiskData(
    val coordinate: Coordinate,
    val dangerLevel: String,
    val dataReliability: Double
)
