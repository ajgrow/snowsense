package in2000.prosjekt.in2000project.data.model


/**
 * Thrown when a station exists but has no observations for the requested period/elements.
 * The ViewModel can use [stationName] to show a contextual error message.
 */
class NoObservationsException(
    val stationName: String?,
    message: String = "No observations available for the requested period"
        + if (stationName != null) " at station $stationName"
          else ""
) : Exception(message)