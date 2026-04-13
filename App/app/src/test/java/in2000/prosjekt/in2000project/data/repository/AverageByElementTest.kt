package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0Observation
import org.junit.Assert.*
import org.junit.Test

class AverageByElementTest {

    @Test
    fun correctMean_multipleValues() {
        val obs = listOf(
            FrostV0Observation(
                "mean(air_temperature P1D)",
                -6.0,
                "degC",
                "PT0H",
                "P1D"),
            FrostV0Observation(
                "mean(air_temperature P1D)",
                -4.0,
                "degC",
                "PT0H",
                "P1D"),
            FrostV0Observation(
                "mean(air_temperature P1D)",
                -2.0,
                "degC",
                "PT0H",
                "P1D"),
        )
        val result = averageByElement(obs)
        assertEquals(-4.0, result["mean(air_temperature P1D)"]!!, 0.001)
    }

    @Test
    fun separatesElements_independentAverages() {
        val obs = listOf(
            FrostV0Observation(
                "mean(air_temperature P1D)",
                -2.0,
                "degC",
                "PT0H",
                "P1D"),
            FrostV0Observation(
                "sum(precipitation_amount P1D)",
                5.0,
                "mm",
                "PT0H",
                "P1D"),
            FrostV0Observation(
                "mean(air_temperature P1D)",
                -4.0,
                "degC",
                "PT0H",
                "P1D"),
            FrostV0Observation(
                "sum(precipitation_amount P1D)",
                3.0,
                "mm",
                "PT0H",
                "P1D"),
        )
        val result = averageByElement(obs)
        assertEquals(-3.0, result["mean(air_temperature P1D)"]!!, 0.001)
        assertEquals(4.0, result["sum(precipitation_amount P1D)"]!!, 0.001)
    }

    @Test
    fun singleObservation_averageEqualsValue() {
        val obs = listOf(
            FrostV0Observation(
                "mean(air_temperature P1D)",
                -3.5, "degC",
                "PT0H",
                "P1D"),
        )
        val result = averageByElement(obs)
        assertEquals(-3.5, result["mean(air_temperature P1D)"]!!, 0.001)
    }

    @Test
    fun emptyInput_returnsEmptyMap() {
        val result = averageByElement(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun allFourElements_returnsAllAverages() {
        // Uses all four real element IDs from FrostClimateRepository.
        val obs = listOf(
            FrostV0Observation(
                "mean(air_temperature P1D)",
                -2.0,
                "degC",
                "PT0H",
                "P1D"),
            FrostV0Observation(
                "sum(precipitation_amount P1D)",
                4.0,
                "mm",
                "PT0H",
                "P1D"),
            FrostV0Observation(
                "mean(wind_speed P1D)",
                3.0,
                "m/s",
                "PT0H",
                "P1D"),
            FrostV0Observation(
                "max(surface_snow_thickness P1D)",
                50.0,
                "cm",
                "PT0H",
                "P1D"),
        )
        val result = averageByElement(obs)
        assertEquals(4, result.size)
        assertEquals(-2.0, result["mean(air_temperature P1D)"]!!, 0.001)
        assertEquals(50.0, result["max(surface_snow_thickness P1D)"]!!, 0.001)
    }
}
