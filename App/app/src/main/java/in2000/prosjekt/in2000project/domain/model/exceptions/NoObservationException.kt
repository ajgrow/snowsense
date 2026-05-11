package in2000.prosjekt.in2000project.domain.model.exceptions

/**
 * Thrown when a station exists but has no observations
 * for the requested period/elements.
 */
class NoObservationsException(
    stationName: String?,
    elements: String?,
    referenceTime: String?,
) : Exception(
    "No observations available at station $stationName"
    + "for $elements with reference time $referenceTime")