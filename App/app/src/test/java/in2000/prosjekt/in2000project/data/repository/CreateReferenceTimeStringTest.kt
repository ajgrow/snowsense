package in2000.prosjekt.in2000project.data.repository

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.MonthDay

class CreateReferenceTimeStringTest {

    @Test
    fun oneYearBack_twoRepetitions() {
        val result = createReferenceTimeString(
            periodStart = MonthDay.of(3, 1),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = 1,
            referenceDate = LocalDate.of(2026, 4, 7),
        )
        assertEquals("R2/2025-03-01/2025-04-01/P1Y", result)
    }

    @Test
    fun yearCrossingPeriod_endDateInNextYear() {
        val result = createReferenceTimeString(
            periodStart = MonthDay.of(12, 15),
            periodEnd = MonthDay.of(1, 15),
            yearsBack = 9,
            referenceDate = LocalDate.of(2026, 4, 7),
        )
        assertEquals("R10/2017-12-15/2018-01-16/P1Y", result)
    }

    @Test
    fun leapYear_plusOneDayStaysInFebruary() {
        val result = createReferenceTimeString(
            periodStart = MonthDay.of(2, 1),
            periodEnd = MonthDay.of(2, 28),
            yearsBack = 1,
            referenceDate = LocalDate.of(2025, 4, 1),
        )
        assertEquals("R2/2024-02-01/2024-02-29/P1Y", result)
    }

    @Test
    fun nonLeapYear_feb29ClampedToFeb28() {
        val result = createReferenceTimeString(
            periodStart = MonthDay.of(2, 29),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = 1,
            referenceDate = LocalDate.of(2024, 2, 1),
        )
        assertEquals("R2/2023-02-28/2023-04-01/P1Y", result)
    }

    @Test
    fun zeroYearsBack_returnsNonRepeatingRange() {
        val result = createReferenceTimeString(
            periodStart = MonthDay.of(3, 1),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = 0,
            referenceDate = LocalDate.of(2026, 4, 7),
        )

        assertEquals("2026-03-01/2026-04-01", result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeYearsBack_throwsIllegalArgument() {
        createReferenceTimeString(
            periodStart = MonthDay.of(3, 1),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = -1,
            referenceDate = LocalDate.of(2026, 4, 7),
        )
    }
}
