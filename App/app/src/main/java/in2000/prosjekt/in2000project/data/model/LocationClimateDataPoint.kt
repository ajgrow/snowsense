package in2000.prosjekt.in2000project.data.model

import in2000.prosjekt.in2000project.data.model.enums.ElementType
import in2000.prosjekt.in2000project.data.model.enums.MeasurementUnit

data class LocationClimateDataPoint(
    val type: ElementType,
    val unit: MeasurementUnit,
    val value: Double?
)
