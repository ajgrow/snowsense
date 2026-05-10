package in2000.prosjekt.in2000project.domain.model

import in2000.prosjekt.in2000project.domain.model.enums.DangerLevel

data class LocationSlabAvalancheRiskData(
    val coordinate: Coordinate,
    val dangerLevel: DangerLevel,
)
