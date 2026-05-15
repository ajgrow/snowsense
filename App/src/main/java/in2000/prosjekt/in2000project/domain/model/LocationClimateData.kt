package in2000.prosjekt.in2000project.domain.model

import java.time.MonthDay

/**
 * Historical daily climate averages for a selected map point, based on the nearest weather station.
 *
 * Values represent the mean of daily observations across multiple years for a specific
 * calendar period. For example: "average daily temperature for Mar 23 – Apr 22, based on
 * 10 years of data."
 *
 * All climate values are nullable — null means the station doesn't measure that element.
 *
 * @param coordinate The coordinate of the location.
 * @param stationName The name of the nearest weather station.
 * @param stationDistanceKm The distance to the nearest weather station in kilometers.
 * @param stationElevationMasl The elevation of the nearest weather station in meters above
 * sea level
 */
data class LocationClimateData(
    // Location
    val coordinate: Coordinate,
    val stationName: String?,
    val stationDistanceKm: Double?,
    val stationElevationMasl: Double?,

    // Period metadata — describes what was queried
    val periodStart: MonthDay,
    val periodEnd: MonthDay,
    val yearsBack: Int,

    // Data quality
    val dataPointCount: Int,
    val yearlyAverages: List<LocationClimateDataPoint>

)