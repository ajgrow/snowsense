package in2000.prosjekt.in2000project.data.repository

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.MonthDay

class BuildRepeatingTimeRangeTest {

    // ── Same-year periods (start and end in the same calendar year) ──

    @Test
    fun sameYearPeriod_currentYearIncluded() {
        val result = buildRepeatingTimeRange(
            periodStart = MonthDay.of(3, 23),
            periodEnd = MonthDay.of(4, 22),
            yearsBack = 9,
            referenceDate = LocalDate.of(2026, 4, 7),
        )
        assertEquals("R10/2017-03-23/2017-04-23/P1Y", result)
    }

    @Test
    fun futurePeriod_currentYearStillIncluded() {
        val result = buildRepeatingTimeRange(
            periodStart = MonthDay.of(7, 1),
            periodEnd = MonthDay.of(7, 31),
            yearsBack = 4,
            referenceDate = LocalDate.of(2026, 4, 7),
        )
        assertEquals("R5/2022-07-01/2022-08-01/P1Y", result)
    }

    @Test
    fun endDateAddsOneDayForHalfOpenInterval() {
        // Frost uses half-open [start, end), so inclusive end is +1 day.
        val result = buildRepeatingTimeRange(
            periodStart = MonthDay.of(4, 1),
            periodEnd = MonthDay.of(4, 30),
            yearsBack = 1,
            referenceDate = LocalDate.of(2026, 5, 1),
        )
        assertEquals("R2/2025-04-01/2025-05-01/P1Y", result)
    }

    // ── Year-crossing periods (start in Dec, end in Jan) ──

    @Test
    fun yearCrossingPeriod_decemberToJanuary() {
        val result = buildRepeatingTimeRange(
            periodStart = MonthDay.of(12, 15),
            periodEnd = MonthDay.of(1, 15),
            yearsBack = 9,
            referenceDate = LocalDate.of(2026, 4, 7),
        )
        assertEquals("R10/2017-12-15/2018-01-16/P1Y", result)
    }

    @Test
    fun yearCrossingPeriod_currentlyInPeriod() {
        val result = buildRepeatingTimeRange(
            periodStart = MonthDay.of(12, 15),
            periodEnd = MonthDay.of(1, 15),
            yearsBack = 2,
            referenceDate = LocalDate.of(2026, 12, 20),
        )
        assertEquals("R3/2024-12-15/2025-01-16/P1Y", result)
    }

    @Test
    fun oneYearBack_twoRepetitions() {
        val result = buildRepeatingTimeRange(
            periodStart = MonthDay.of(3, 1),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = 1,
            referenceDate = LocalDate.of(2026, 4, 7),
        )
        assertEquals("R2/2025-03-01/2025-04-01/P1Y", result)
    }

    // ── Leap year edge cases ──

    @Test
    fun leapYearBoundary_periodEndingFeb28() {
        // The end date +1 day lands on Feb 29 when oldest year is a leap year.
        val result = buildRepeatingTimeRange(
            periodStart = MonthDay.of(2, 1),
            periodEnd = MonthDay.of(2, 28),
            yearsBack = 1,
            referenceDate = LocalDate.of(2025, 4, 1),
        )
        assertEquals("R2/2024-02-01/2024-02-29/P1Y", result)
    }

    @Test
    fun nonLeapYearBoundary_periodEndingFeb28() {
        // The end date +1 day should land on Mar 1 when oldest year is non-leap.
        val result = buildRepeatingTimeRange(
            periodStart = MonthDay.of(2, 1),
            periodEnd = MonthDay.of(2, 28),
            yearsBack = 1,
            referenceDate = LocalDate.of(2026, 4, 1),
        )
        assertEquals("R2/2025-02-01/2025-03-01/P1Y", result)
    }

    @Test
    fun nonleapYearBoundaryClamping_periodEndingFeb29() {
        // Should land on Mar 1 without any issues despite choosing Feb 29.
        val result = buildRepeatingTimeRange(
            periodStart = MonthDay.of(2, 1),
            periodEnd = MonthDay.of(2, 29),
            yearsBack = 1,
            referenceDate = LocalDate.of(2024, 2, 1),
        )
        assertEquals("R2/2023-02-01/2023-03-01/P1Y", result)
    }

    @Test
    fun nonleapYearBoundaryClamping_periodStartingFeb29() {
        // Should clamp to Feb 28 when oldest year is non-leap.
        val result = buildRepeatingTimeRange(
            periodStart = MonthDay.of(2, 29),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = 1,
            referenceDate = LocalDate.of(2024, 2, 1),
        )
        assertEquals("R2/2023-02-28/2023-04-01/P1Y", result)
    }

    // ── yearsBack negative test, equivalence partitioning and boundary values  ──

    @Test(expected = IllegalArgumentException::class)
    fun negativeYearsBack_throwsIllegalArgument() {
        buildRepeatingTimeRange(
            periodStart = MonthDay.of(3, 1),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = -1,
            referenceDate = LocalDate.of(2026, 4, 7),
        )
    }

    @Test
    fun zeroYearsBack_returnsOneRepetition() {
        val result = buildRepeatingTimeRange(
            periodStart = MonthDay.of(3, 1),
            periodEnd = MonthDay.of(3, 31),
            yearsBack = 0,
            referenceDate = LocalDate.of(2026, 4, 7),
        )

        assertEquals("R1/2026-03-01/2026-04-01/P1Y", result)
    }
}
