package in2000.prosjekt.in2000project.data.model

// This data class represents one ski touring area.
// Each area has a name and some placeholder climate info that we will use when we have frost
data class SkiArea(
    val id: String,
    val name: String,
    val averageSnowfall: String,
    val averagePrecipitation: String,
    val dailyMeanTemperature: String
)


fun LocationClimateData.toSkiArea() = SkiArea(
    id = "${coordinate.latitude},${coordinate.longitude}",
    name = stationName ?: "Selected Area",
    averageSnowfall = avgDailyMaxSnowDepthCm
        ?.let { "%.1f cm daily average".format(it) }
        ?: "No data",
    averagePrecipitation = avgDailyPrecipitationMm
        ?.let { "%.1f mm daily average".format(it) }
        ?: "No data",
    dailyMeanTemperature = avgDailyMeanTemperatureC
        ?.let { "%.1f°C daily average".format(it) }
        ?: "No data"
)