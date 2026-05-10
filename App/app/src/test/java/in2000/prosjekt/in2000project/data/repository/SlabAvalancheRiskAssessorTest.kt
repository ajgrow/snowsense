package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.domain.model.DailyWeatherRecord
import in2000.prosjekt.in2000project.domain.model.enums.DangerLevel
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class SlabAvalancheRiskAssessorTest {

    private fun record(
        daysAgo: Int,
        temp: Double? = null,
        precip: Double? = null,
    ) = DailyWeatherRecord(
        date = LocalDate.of(2026, 4, 1)
                        .minusDays(daysAgo.toLong()),
        tempC = temp,
        precipMm = precip,
    )

    @Test
    fun normalize_atLow_returnsZero() {
        val result = SlabAvalancheRiskAssessor.normalize(
            10.0, 10.0, 60.0,
        )
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun normalize_midpoint_returnsHalf() {
        val result = SlabAvalancheRiskAssessor.normalize(
            35.0, 10.0, 60.0,
        )
        assertEquals(0.5, result, 0.001)
    }

    @Test
    fun normalize_aboveHigh_clampedToOne() {
        val result = SlabAvalancheRiskAssessor.normalize(
            100.0, 10.0, 60.0,
        )
        assertEquals(1.0, result, 0.001)
    }

    @Test
    fun recentLoading_warmDays_returnsZero() {
        val days = listOf(
            record(2, temp = 2.0, precip = 30.0),
            record(1, temp = 2.0, precip = 30.0),
            record(0, temp = 2.0, precip = 30.0),
        )
        val result =
            SlabAvalancheRiskAssessor.calcRecentLoading(days)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun recentLoading_midRange_returnsExpected() {
        val days = listOf(
            record(2, temp = -1.0, precip = 12.0),
            record(1, temp = -1.0, precip = 12.0),
            record(0, temp = -1.0, precip = 12.0),
        )
        val result =
            SlabAvalancheRiskAssessor.calcRecentLoading(days)
        assertEquals(0.52, result, 0.001)
    }

    @Test
    fun recentLoading_nullTempExcluded() {
        // Null-temp day should be skipped, so only 60mm counted.
        val days = listOf(
            record(2, temp = null, precip = 30.0),
            record(1, temp = -1.0, precip = 30.0),
            record(0, temp = -1.0, precip = 30.0),
        )
        val result =
            SlabAvalancheRiskAssessor.calcRecentLoading(days)
        assertEquals(1.0, result, 0.001)
    }

    @Test
    fun rapidWarming_cooling_returnsZero() {
        val days = listOf(
            record(2, temp = 5.0),
            record(1, temp = 3.0),
            record(0, temp = -2.0),
        )
        val result =
            SlabAvalancheRiskAssessor.calcRapidWarming(days)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun rapidWarming_strongWarming_returnsExpected() {
        val days = listOf(
            record(2, temp = -5.0),
            record(1, temp = -3.0),
            record(0, temp = 5.0),
        )
        val result =
            SlabAvalancheRiskAssessor.calcRapidWarming(days)
        assertEquals(0.875, result, 0.001)
    }

    @Test
    fun structural_noColdDays_returnsZero() {
        val days = listOf(
            record(10, temp = 0.0, precip = 20.0),
            record(5, temp = 2.0, precip = 20.0),
            record(0, temp = 5.0, precip = 20.0),
        )
        val result =
            SlabAvalancheRiskAssessor.calcStructural(days)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun structural_weakLayerNotBuried_returnsZero() {
        val days = listOf(
            record(2, temp = -8.0, precip = 5.0),
            record(1, temp = -8.0, precip = 5.0),
            record(0, temp = -8.0, precip = 5.0),
        )
        val result =
            SlabAvalancheRiskAssessor.calcStructural(days)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun structural_weakLayerTooThinBeforeWarm_returnsZero() {
        val days = listOf(
            record(10, temp = -8.0, precip = 0.5),
            record(5, temp = 0.0, precip = 20.0),
            record(0, temp = 0.0, precip = 20.0),
        )
        val result =
            SlabAvalancheRiskAssessor.calcStructural(days)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun structural_confirmedWeakLayerWithSlab_returnsPositive() {
        val days = listOf(
            record(15, temp = -8.0, precip = 2.0),
            record(10, temp = -2.0, precip = 12.0),
            record(5, temp = -2.0, precip = 25.0),
        )
        val result =
            SlabAvalancheRiskAssessor.calcStructural(days)
        assertEquals(0.822, result, 0.01)
    }

    @Test
    fun structural_coldInterruptsSlabBeforeBackbone_resets() {
        val days = listOf(
            record(20, temp = -8.0, precip = 2.0),
            record(15, temp = -2.0, precip = 5.0),
            record(10, temp = -6.0, precip = 3.0),
        )
        val result =
            SlabAvalancheRiskAssessor.calcStructural(days)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun structural_coldConfirmsBackbone_slabAccumulates() {
        val days = listOf(
            record(20, temp = -8.0, precip = 2.0),
            record(15, temp = -2.0, precip = 12.0),
            record(10, temp = -6.0, precip = 0.0),
            record(5, temp = -2.0, precip = 20.0),
        )
        val result =
            SlabAvalancheRiskAssessor.calcStructural(days)
        assertEquals(0.742, result, 0.01)
    }

    @Test
    fun assess_emptyInput_returnsUnknown() {
        assertEquals(
            DangerLevel.UNKNOWN,
            SlabAvalancheRiskAssessor.assess(emptyList()),
        )
    }

    @Test
    fun assess_triggersWithoutStructure_returnsLow() {
        val days = listOf(
            record(2, temp = -2.0, precip = 30.0),
            record(1, temp = -2.0, precip = 30.0),
            record(0, temp = -2.0, precip = 30.0),
        )
        assertEquals(
            DangerLevel.LOW,
            SlabAvalancheRiskAssessor.assess(days),
        )
    }

    @Test
    fun assess_structureWithoutTrigger_returnsModerate() {
        val days = listOf(
            record(15, temp = -8.0, precip = 2.0),
            record(10, temp = -2.0, precip = 12.0),
            record(5, temp = -2.0, precip = 25.0),
            record(2, temp = 2.0, precip = 0.0),
            record(1, temp = 2.0, precip = 0.0),
            record(0, temp = 2.0, precip = 0.0),
        )
        assertEquals(
            DangerLevel.MODERATE,
            SlabAvalancheRiskAssessor.assess(days),
        )
    }

    @Test
    fun assess_structureWithModestTrigger_returnsConsiderable() {
        val days = listOf(
            record(20, temp = -8.0, precip = 5.0),
            record(15, temp = -2.0, precip = 12.0),
            record(10, temp = -2.0, precip = 80.0),
            record(2, temp = -1.0, precip = 6.0),
            record(1, temp = -1.0, precip = 6.0),
            record(0, temp = -1.0, precip = 6.0),
        )
        assertEquals(
            DangerLevel.CONSIDERABLE,
            SlabAvalancheRiskAssessor.assess(days),
        )
    }

    @Test
    fun assess_structureWithStrongTrigger_returnsHigh() {
        val days = listOf(
            record(20, temp = -8.0, precip = 5.0),
            record(15, temp = -2.0, precip = 12.0),
            record(10, temp = -2.0, precip = 80.0),
            record(2, temp = -1.0, precip = 12.0),
            record(1, temp = -1.0, precip = 12.0),
            record(0, temp = -1.0, precip = 12.0),
        )
        assertEquals(
            DangerLevel.HIGH,
            SlabAvalancheRiskAssessor.assess(days),
        )
    }

    @Test
    fun assess_extremeConditions_returnsVeryHigh() {
        val days = listOf(
            record(20, temp = -8.0, precip = 5.0),
            record(15, temp = -2.0, precip = 12.0),
            record(10, temp = -2.0, precip = 80.0),
            record(2, temp = -5.0, precip = 20.0),
            record(1, temp = -3.0, precip = 20.0),
            record(0, temp = 5.0, precip = 20.0),
        )
        assertEquals(
            DangerLevel.VERY_HIGH,
            SlabAvalancheRiskAssessor.assess(days),
        )
    }

    @Test
    fun assess_unsortedInput_handledCorrectly() {
        val sorted = listOf(
            record(15, temp = -8.0, precip = 2.0),
            record(10, temp = -2.0, precip = 12.0),
            record(5, temp = -2.0, precip = 25.0),
            record(2, temp = 2.0, precip = 0.0),
            record(1, temp = 2.0, precip = 0.0),
            record(0, temp = 2.0, precip = 0.0),
        )
        val shuffled = sorted.shuffled()
        assertEquals(
            SlabAvalancheRiskAssessor.assess(sorted),
            SlabAvalancheRiskAssessor.assess(shuffled),
        )
    }
}
