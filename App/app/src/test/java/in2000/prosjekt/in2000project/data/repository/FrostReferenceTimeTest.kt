package in2000.prosjekt.in2000project.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.MonthDay

class FrostReferenceTimeTest {

    // .fromMonthDayRange()

    @Test
    fun oneYearBack_twoRepetitions() {
        val result = FrostReferenceTime.fromMonthDayRange(
            periodStart = MonthDay.of(3, 1),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = 1,
            referenceDate = LocalDate.of(2026, 4, 7),
        ).value
        assertEquals("R2/2025-03-01/2025-04-01/P1Y", result)
    }

    @Test
    fun yearCrossingPeriod_endDateInNextYear() {
        val result = FrostReferenceTime.fromMonthDayRange(
            periodStart = MonthDay.of(12, 15),
            periodEnd = MonthDay.of(1, 15),
            yearsBack = 9,
            referenceDate = LocalDate.of(2026, 4, 7),
        ).value
        assertEquals("R10/2017-12-15/2018-01-16/P1Y", result)
    }

    @Test
    fun samePeriodStartAndEnd_returnsSingleDayRange() {
        val result = FrostReferenceTime.fromMonthDayRange(
            periodStart = MonthDay.of(3, 15),
            periodEnd = MonthDay.of(3, 15),
            yearsBack = 0,
            referenceDate = LocalDate.of(2026, 4, 7),
        ).value
        assertEquals("2026-03-15/2026-03-16", result)
    }

    @Test
    fun leapYear_plusOneDayStaysInFebruary() {
        val result = FrostReferenceTime.fromMonthDayRange(
            periodStart = MonthDay.of(2, 1),
            periodEnd = MonthDay.of(2, 28),
            yearsBack = 1,
            referenceDate = LocalDate.of(2025, 4, 1),
        ).value
        assertEquals("R2/2024-02-01/2024-02-29/P1Y", result)
    }

    @Test
    fun nonLeapYear_feb29ClampedToFeb28() {
        val result = FrostReferenceTime.fromMonthDayRange(
            periodStart = MonthDay.of(2, 29),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = 1,
            referenceDate = LocalDate.of(2024, 2, 1),
        ).value
        assertEquals("R2/2023-02-28/2023-04-01/P1Y", result)
    }

    @Test
    fun zeroYearsBack_returnsNonRepeatingRange() {
        val result = FrostReferenceTime.fromMonthDayRange(
            periodStart = MonthDay.of(3, 1),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = 0,
            referenceDate = LocalDate.of(2026, 4, 7),
        ).value

        assertEquals("2026-03-01/2026-04-01", result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeYearsBack_throwsIllegalArgument() {
        FrostReferenceTime.fromMonthDayRange(
            periodStart = MonthDay.of(3, 1),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = -1,
            referenceDate = LocalDate.of(2026, 4, 7),
        )
    }

    // .of()

    @Test
    fun singlePeriod_returnsNonRepeatingRange() {
        val result = FrostReferenceTime.of(
            startDate = LocalDate.of(2025, 3, 1),
            endDate = LocalDate.of(2025, 3, 31),
        ).value
        assertEquals("2025-03-01/2025-04-01", result)
    }

    @Test
    fun multiplePeriods_returnsRepeatingRange() {
        val result = FrostReferenceTime.of(
            startDate = LocalDate.of(2025, 3, 1),
            endDate = LocalDate.of(2025, 3, 31),
            totalPeriods = 3,
        ).value
        assertEquals("R3/2025-03-01/2025-04-01/P1Y", result)
    }

    @Test
    fun sameDates_returnsSingleDayRange() {
        val result = FrostReferenceTime.of(
            startDate = LocalDate.of(2025, 3, 15),
            endDate = LocalDate.of(2025, 3, 15),
        ).value
        assertEquals("2025-03-15/2025-03-16", result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun startAfterEnd_throwsIllegalArgument() {
        FrostReferenceTime.of(
            startDate = LocalDate.of(2025, 4, 1),
            endDate = LocalDate.of(2025, 3, 1),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun zeroTotalPeriods_throwsIllegalArgument() {
        FrostReferenceTime.of(
            startDate = LocalDate.of(2025, 3, 1),
            endDate = LocalDate.of(2025, 3, 31),
            totalPeriods = 0,
        )
    }
}
