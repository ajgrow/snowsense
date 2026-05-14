package in2000.prosjekt.in2000project.domain.model

import in2000.prosjekt.in2000project.domain.model.enums.ElementType

/**
 * Data class representing a climate area.
 *
 * @property stationName The name of the station.
 * @property temperatureData The temperature data.
 * @property precipitationData The precipitation data.
 * @property windSpeedData The wind speed data.
 */
data class ClimateArea(
    val stationName: String?,
    val temperatureData: List<LocationClimateDataPoint>,
    val precipitationData: List<LocationClimateDataPoint>,
    val windSpeedData: List<LocationClimateDataPoint>,
    val snowDepthData: List<LocationClimateDataPoint>,
)

/**
 * Extension function to convert a [LocationClimateData] to a [ClimateArea].
 */
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