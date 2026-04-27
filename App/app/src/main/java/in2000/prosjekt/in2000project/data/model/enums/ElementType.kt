package in2000.prosjekt.in2000project.data.model.enums

enum class ElementType(
    val frostElementId: String,
    val defaultUnit: MeasurementUnit,
) {
    TEMPERATURE_MEAN_P1D(
        "mean(air_temperature P1D)",
        MeasurementUnit.CELSIUS
    ),
    PRECIPITATION_SUM_P1D(
        "sum(precipitation_amount P1D)",
        MeasurementUnit.MILLIMETERS
    ),
    WIND_SPEED_MEAN_P1D(
        "mean(wind_speed P1D)",
        MeasurementUnit.METERS_PER_SECOND
    ),
    SNOW_DEPTH_MAX_P1D(
        "max(surface_snow_thickness P1D)",
        MeasurementUnit.CENTIMETERS
    ),
}