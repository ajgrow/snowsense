package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.domain.model.DailyWeatherRecord
import in2000.prosjekt.in2000project.domain.model.enums.DangerLevel
import kotlin.math.sqrt

// Estimates slab avalanche danger from daily weather observations.
object SlabAvalancheRiskAssessor {

    const val LOOKBACK_DAYS = 30
    private const val RECENT_DAYS = 3

    private const val RECENT_LOADING_LOW = 10.0
    private const val RECENT_LOADING_HIGH = 60.0

    private const val RAPID_WARMING_LOW = 2.0
    private const val RAPID_WARMING_HIGH = 10.0

    private const val WEAK_LAYER_LOW = 0.5
    private const val WEAK_LAYER_HIGH = 2.0

    private const val SLAB_BACKBONE_MIN_LWE = 10.0
    private const val SLAB_LOAD_LOW = 10.0
    private const val SLAB_LOAD_HIGH = 50.0

    private const val STRUCTURAL_BASELINE = 0.24 // structure gives Moderate
    private const val THRESHOLD_MODERATE = 0.10
    private const val THRESHOLD_CONSIDERABLE = 0.25
    private const val THRESHOLD_HIGH = 0.50
    private const val THRESHOLD_VERY_HIGH = 0.75

    fun assess(days: List<DailyWeatherRecord>): DangerLevel {
        if (days.isEmpty() || days.size < RECENT_DAYS)
            return DangerLevel.UNKNOWN

        val sorted = days.sortedBy { it.date }
        val recent = sorted.takeLast(RECENT_DAYS)

        val structural = calcStructural(sorted)
        val trigger = calcTrigger(recent)
        val score = (structural
                * (STRUCTURAL_BASELINE
                    + (1.0 - STRUCTURAL_BASELINE)
                        * trigger))

        return when {
            score >= THRESHOLD_VERY_HIGH -> DangerLevel.VERY_HIGH
            score >= THRESHOLD_HIGH -> DangerLevel.HIGH
            score >= THRESHOLD_CONSIDERABLE -> DangerLevel.CONSIDERABLE
            score >= THRESHOLD_MODERATE -> DangerLevel.MODERATE
            else -> DangerLevel.LOW
        }
    }

    // Walks through days tracking weak layer formation then slab build-up.
    // Returns 0 if either component is missing.
    internal fun calcStructural(allDays: List<DailyWeatherRecord>): Double {
        var weakLayerLwe = 0.0
        var weakLayerConfirmed = false
        var slabLwe = 0.0
        var slabBackboneConfirmed = false

        for (day in allDays) {
            val temp = day.tempC ?: continue
            val precip = day.precipMm ?: 0.0

            if (!weakLayerConfirmed) {
                if (temp <= -5.0)
                    weakLayerLwe += precip
                else if (weakLayerLwe >= 1.0) {
                    weakLayerConfirmed = true
                    slabLwe = precip
                } else
                    weakLayerLwe = 0.0

            } else if (!slabBackboneConfirmed) {
                if (temp > -5.0)
                    slabLwe += precip
                else if (slabLwe >= SLAB_BACKBONE_MIN_LWE) {
                    slabBackboneConfirmed = true
                    slabLwe += precip
                } else
                    slabLwe = 0.0

            } else {
                slabLwe += precip
            }
        }

        if (!weakLayerConfirmed || slabLwe < SLAB_BACKBONE_MIN_LWE )
            return 0.0

        val weakLayerFactor = normalize(
            weakLayerLwe,
            WEAK_LAYER_LOW,
            WEAK_LAYER_HIGH)

        val slabFactor = normalize(
            slabLwe,
            SLAB_LOAD_LOW,
            SLAB_LOAD_HIGH)

        return sqrt(weakLayerFactor * slabFactor)
    }

    // Combines loading and warming triggers so either one can elevate risk.
    internal fun calcTrigger(
        recentDays: List<DailyWeatherRecord>
    ): Double {
        val loading = calcRecentLoading(recentDays)
        val warming = calcRapidWarming(recentDays)
        return 1.0 - (1.0 - loading) * (1.0 - warming)
    }

    internal fun calcRecentLoading(
        recentDays: List<DailyWeatherRecord>
    ): Double {
        val recentColdLwe = recentDays.sumOf { day ->
            if (day.tempC != null && day.tempC <= 0.0)
                (day.precipMm ?: 0.0)
            else 0.0
        }

        return normalize(
            recentColdLwe,
            RECENT_LOADING_LOW,
            RECENT_LOADING_HIGH)
    }

    internal fun calcRapidWarming(recentDays: List<DailyWeatherRecord>
    ): Double {
        val temps = recentDays.mapNotNull { it.tempC }

        if (temps.size < 2)
            return 0.0

        val warmingDelta = (temps.last() - temps.dropLast(1).average())
            .coerceAtLeast(0.0)

        return normalize(
            warmingDelta,
            RAPID_WARMING_LOW,
            RAPID_WARMING_HIGH)
    }

    // Clamps value into [0, 1] between low and high.
    internal fun normalize(value: Double, low: Double, high: Double): Double =
        ((value - low) / (high - low)).coerceIn(0.0, 1.0)
}
