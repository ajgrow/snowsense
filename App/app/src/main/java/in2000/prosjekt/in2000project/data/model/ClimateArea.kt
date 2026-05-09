package in2000.prosjekt.in2000project.data.model

import in2000.prosjekt.in2000project.data.model.enums.ElementType

// This data class represents one ski touring area.
// It now includes historical climate data separated by element type for graphing.
data class ClimateArea(
    val stationName: String?,
    val temperatureData: List<LocationClimateDataPoint>,
    val precipitationData: List<LocationClimateDataPoint>,
    val windSpeedData: List<LocationClimateDataPoint>,
    val snowDepthData: List<LocationClimateDataPoint>,
)

fun LocationClimateData.toClimateArea() = ClimateArea(
    stationName = stationName,
    temperatureData = yearlyAverages
        .filter { it.type == ElementType.TEMPERATURE_MEAN_P1D }
        .sortedBy { it.year },

    precipitationData = yearlyAverages
        .filter { it.type == ElementType.PRECIPITATION_SUM_P1D }
        .sortedBy { it.year },

    windSpeedData = yearlyAverages
        .filter { it.type == ElementType.WIND_SPEED_MEAN_P1D }
        .sortedBy { it.year },

    snowDepthData = yearlyAverages
        .filter { it.type == ElementType.SNOW_DEPTH_MAX_P1D }
        .sortedBy { it.year }
)