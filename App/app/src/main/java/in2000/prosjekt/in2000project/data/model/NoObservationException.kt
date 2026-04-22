package in2000.prosjekt.in2000project.data.model

import java.time.MonthDay


/**
 * Thrown when a station exists but has no observations for the requested period/elements.
 * The ViewModel can use [stationName], [startDate], [endDate], and [yearsBack] to show a contextual error message.
 */
class NoObservationsException(
    val stationName: String?,
    val periodStart: MonthDay,
    val periodEnd: MonthDay,
    val yearsBack: Int,
    message: String = "No observations available"
        + if (stationName != null) " at station $stationName "
          else " "
        + "between $periodStart to $periodEnd over the last $yearsBack years",
) : Exception(message)