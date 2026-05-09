package in2000.prosjekt.in2000project.domain.model

import in2000.prosjekt.in2000project.domain.model.enums.ElementType
import in2000.prosjekt.in2000project.domain.model.enums.MeasurementUnit

data class LocationClimateDataPoint(
    val year: Int,
    val type: ElementType,
    val unit: MeasurementUnit,
    val avgValue: Double,
)
