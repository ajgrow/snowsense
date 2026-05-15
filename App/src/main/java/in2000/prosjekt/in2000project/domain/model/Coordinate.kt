package in2000.prosjekt.in2000project.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a geographic coordinate.
 *
 * @param latitude Latitude in decimal degrees, range -90 to 90.
 * @param longitude Longitude in decimal degrees, range -180 to 180.
 */
@Serializable
data class Coordinate(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0)
        { "Coordinate: Latitude must be in [-90, 90], was $latitude" }

        require(longitude in -180.0..180.0)
        { "Coordinate: Longitude must be in [-180, 180], was $longitude" }
    }
}
