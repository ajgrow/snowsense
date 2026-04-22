package in2000.prosjekt.in2000project.data.model.enums

enum class ElementType(val description: String, val unit: MeasurementUnit) {
    MEAN_TEMPERATURE("Mean temperature", MeasurementUnit.CELSIUS),
    PRECIPITATION("Precipitation", MeasurementUnit.MILLIMETERS),
    WIND_SPEED("Wind Speed", MeasurementUnit.METERS_PER_SECOND),
    SNOW_DEPTH("Snow Depth", MeasurementUnit.CENTIMETERS)
}