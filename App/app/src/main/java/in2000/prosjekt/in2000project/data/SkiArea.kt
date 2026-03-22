package in2000.prosjekt.in2000project.data

// This data class represents one ski touring area.
// Each area has a name and some placeholder climate info that we will use when we have frost
data class SkiArea(
    val id: String,
    val name: String,
    val averageSnowfall: String,
    val averageRainfall: String,
    val averageWind: String,
    val averageTemperature: String,
    val guideNotes: String
)
