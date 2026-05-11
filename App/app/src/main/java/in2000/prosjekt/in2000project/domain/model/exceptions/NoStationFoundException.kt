package in2000.prosjekt.in2000project.domain.model.exceptions

import in2000.prosjekt.in2000project.domain.model.Coordinate


/**
 * Thrown when no station found near the location.
 * @property coordinate The coordinate for which no station was found.
 */
class NoStationFoundException(
    val coordinate: Coordinate? = null,
    message: String = "No station available for the requested location"
                    + if (coordinate != null)
                        ": (${coordinate.latitude}, ${coordinate.longitude})"
                    else ""
) : Exception(message)