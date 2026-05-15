package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.repository.FrostReferenceTime.Companion.fromMonthDayRange
import in2000.prosjekt.in2000project.data.repository.FrostReferenceTime.Companion.of
import java.time.LocalDate
import java.time.MonthDay

/**
 * A validated Frost v0 API `referenceTime` string.
 *
 * Wraps the repeating-interval format `R{n}/{start}/{end}/P1Y`, e.g.
 * `R10/2017-03-23/2017-04-23/P1Y` — data for Mar 23–Apr 22 repeated yearly for 10 years.
 *
 * Instances are created via [of] or [fromMonthDayRange]; the [value] is the formatted string
 * ready to pass directly to the Frost API.
 */
@JvmInline
value class FrostReferenceTime private constructor(val value: String) {

    companion object {

        /**
         * Creates a [FrostReferenceTime] from explicit date bounds.
         *
         * [endDate] is inclusive but is converted to next day in the output
         * to match the Frost API's half-open interval.
         *
         * When [totalPeriods] is 1, produces a simple date range (`start/end`).
         * When greater than 1, produces a repeating interval (`R{n}/start/end/P1Y`).
         *
         * @throws IllegalArgumentException if [startDate] is not before [endDate]
         *   or [totalPeriods] is less than 1.
         */
        fun of(
            startDate: LocalDate,
            endDate: LocalDate,
            totalPeriods: Int = 1,
        ): FrostReferenceTime {
            require(startDate <= endDate) {
                "startDate can't be after endDate"
            }
            require(totalPeriods >= 1) {
                "totalPeriods must be at least 1"
            }

            val endDate = endDate.plusDays(1).toString()

            if (totalPeriods == 1)
                return FrostReferenceTime("$startDate/$endDate")

            return FrostReferenceTime(
                "R$totalPeriods/$startDate/$endDate/P1Y"
            )
        }

        /**
         * Creates a [FrostReferenceTime] from month-day bounds, repeating yearly
         * across the specified number of past years.
         *
         * Handles periods crossing the year boundary (e.g. Dec 15 – Jan 15).
         *
         * @throws IllegalArgumentException if [yearsBack] is negative.
         */
        fun fromMonthDayRange(
            periodStart: MonthDay,
            periodEnd: MonthDay,
            yearsBack: Int = 0,
            referenceDate: LocalDate = LocalDate.now(),
        ): FrostReferenceTime {
            require(yearsBack >= 0) {
                "yearsBack must be non-negative"
            }

            val startYear = referenceDate.year - yearsBack

            val endYear = if (periodStart > periodEnd)
                startYear + 1
            else startYear

            return of(
                periodStart.atYear(startYear),
                periodEnd.atYear(endYear),
                yearsBack + 1,
            )
        }
    }
}
