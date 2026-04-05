package in2000.prosjekt.in2000project.data.model

/**
 * Climate statistics for a selected map point, based on the nearest weather station.
 * All climate values are nullable — null means the station doesn't measure that element.
 *
 * @property coordinate The map point the user selected (WGS-84).
 * @property stationName Name of the nearest Frost weather station.
 * @property stationDistanceKm Distance in km from the selected point to the station.
 * @property stationElevationMasl Station elevation in meters above sea level.
 * @property meanTemperatureC Average of monthly mean temperatures across queried winters (°C).
 * @property meanPrecipitationMm Average monthly precipitation total (mm).
 * @property meanWindSpeedMs Average monthly wind speed (m/s).
 * @property maxSnowDepthCm Average of monthly max snow depth (cm). Many stations lack this.
 * @property dataYearsCount Number of winters that had data (e.g. "based on 5 winters").
 * @property elementErrors Description of missing elements, e.g. "Snow depth not available".
 */
data class LocationClimateData(
    val coordinate: Coordinate,
    val stationName: String?,
    val stationDistanceKm: Double?,
    val stationElevationMasl: Double?,
    val meanTemperatureC: Double?,
    val meanPrecipitationMm: Double?,
    val meanWindSpeedMs: Double?,
    val maxSnowDepthCm: Double?,
    val dataYearsCount: Int,
    val elementErrors: String?
)