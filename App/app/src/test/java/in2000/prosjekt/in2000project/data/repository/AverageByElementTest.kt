package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.makeObservation
import org.junit.Assert.*
import org.junit.Test

class AverageByElementTest {

    @Test
    fun correctMean_multipleValues() {
        val obs = listOf(
            makeObservation(value = -6.0),
            makeObservation(value = -4.0),
            makeObservation(value = -2.0),
        )
        val result = averageByElement(obs)
        assertEquals(-4.0, result["mean(air_temperature P1D)"]!!, 0.001)
    }

    @Test
    fun separatesElements_independentAverages() {
        val obs = listOf(
            makeObservation(value = -2.0),
            makeObservation(elementId = "sum(precipitation_amount P1D)", value = 5.0, unit = "mm"),
            makeObservation(value = -4.0),
            makeObservation(elementId = "sum(precipitation_amount P1D)", value = 3.0, unit = "mm"),
        )
        val result = averageByElement(obs)
        assertEquals(-3.0, result["mean(air_temperature P1D)"]!!, 0.001)
        assertEquals(4.0, result["sum(precipitation_amount P1D)"]!!, 0.001)
    }

    @Test
    fun singleObservation_averageEqualsValue() {
        val obs = listOf(
            makeObservation(value = -3.5),
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
            makeObservation(value = -2.0),
            makeObservation(elementId = "sum(precipitation_amount P1D)", value = 4.0, unit = "mm"),
            makeObservation(elementId = "mean(wind_speed P1D)", value = 3.0, unit = "m/s"),
            makeObservation(elementId = "max(surface_snow_thickness P1D)", value = 50.0, unit = "cm"),
        )
        val result = averageByElement(obs)
        assertEquals(4, result.size)
        assertEquals(-2.0, result["mean(air_temperature P1D)"]!!, 0.001)
        assertEquals(50.0, result["max(surface_snow_thickness P1D)"]!!, 0.001)
    }
}
