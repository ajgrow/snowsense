package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.FakeFrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.makeDay
import in2000.prosjekt.in2000project.data.datasource.makeObsResponse
import in2000.prosjekt.in2000project.data.datasource.makeSourceResponse
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.DangerLevelType
import in2000.prosjekt.in2000project.data.model.NoObservationsException
import in2000.prosjekt.in2000project.data.model.NoStationFoundException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class SlabAvalancheRiskTest {

    private lateinit var fakeDataSource: FakeFrostV0DataSource
    private lateinit var repository: FrostClimateRepository

    @Before
    fun setUp() {
        fakeDataSource = FakeFrostV0DataSource()
        repository = FrostClimateRepository(fakeDataSource)
    }

    @Test
    fun stableTemperatureAndNoSnow_shouldGiveLow() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = 0.0, precip = 0.0),
            makeDay("2026-03-31T00:00:00Z", temp = 0.0, precip = 0.0),
            makeDay("2026-03-30T00:00:00Z", temp = 0.0, precip = 0.0),
        )

        val result = repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
        assertEquals(DangerLevelType.LOW, result.dangerLevel.type)
    }

    @Test
    fun lightSnowfallAndColdWeather_shouldGiveLow() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = -5.0, precip = 5.0),
            makeDay("2026-03-31T00:00:00Z", temp = -5.0, precip = 5.0),
            makeDay("2026-03-30T00:00:00Z", temp = -5.0, precip = 5.0),
        )

        val result = repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
        assertEquals(DangerLevelType.LOW, result.dangerLevel.type)
    }

    @Test
    fun onlyWarmingNoSnowpack_shouldGiveLow() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = 10.0, precip = 0.0),
            makeDay("2026-03-31T00:00:00Z", temp = -5.0, precip = 0.0),
            makeDay("2026-03-30T00:00:00Z", temp = -5.0, precip = 0.0),
        )

        val result = repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
        assertEquals(DangerLevelType.LOW, result.dangerLevel.type)
    }

    @Test
    fun heavySnowfallButStableCold_shouldGiveModerateAtMost() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = -5.0, precip = 25.0),
            makeDay("2026-03-31T00:00:00Z", temp = -5.0, precip = 25.0),
            makeDay("2026-03-30T00:00:00Z", temp = -5.0, precip = 25.0),
            makeDay("2026-03-29T00:00:00Z", temp = -5.0, precip = 25.0),
        )

        val result = repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
        assertEquals(DangerLevelType.LOW, result.dangerLevel.type)
    }

    @Test
    fun coldPeriodThenMildWithSomePrecip_shouldGiveConsiderable() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-03-25T00:00:00Z", temp = -8.0, precip = 8.0),
            makeDay("2026-03-26T00:00:00Z", temp = -8.0, precip = 8.0),
            makeDay("2026-03-27T00:00:00Z", temp = -8.0, precip = 8.0),
            makeDay("2026-03-30T00:00:00Z", temp = -2.0, precip = 12.0),
            makeDay("2026-03-31T00:00:00Z", temp = -2.0, precip = 12.0),
            makeDay("2026-04-01T00:00:00Z", temp = 2.0, precip = 15.0),
        )

        val result = repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
        assertEquals(DangerLevelType.CONSIDERABLE, result.dangerLevel.type)
    }

    @Test
    fun coldPeriodThenWarmDayWithHeavyPrecip_shouldGiveHigh() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-03-22T00:00:00Z", temp = -10.0, precip = 10.0),
            makeDay("2026-03-23T00:00:00Z", temp = -10.0, precip = 10.0),
            makeDay("2026-03-24T00:00:00Z", temp = -10.0, precip = 10.0),
            makeDay("2026-03-30T00:00:00Z", temp = -3.0, precip = 20.0),
            makeDay("2026-03-31T00:00:00Z", temp = -3.0, precip = 20.0),
            makeDay("2026-04-01T00:00:00Z", temp = 5.0, precip = 15.0),
        )

        val result = repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
        assertEquals(DangerLevelType.HIGH, result.dangerLevel.type)
    }

    @Test
    fun extremeWinterStormThenMassiveWarming_shouldGiveVeryHigh() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-03-20T00:00:00Z", temp = -12.0, precip = 20.0),
            makeDay("2026-03-21T00:00:00Z", temp = -12.0, precip = 20.0),
            makeDay("2026-03-22T00:00:00Z", temp = -12.0, precip = 20.0),
            makeDay("2026-03-23T00:00:00Z", temp = -12.0, precip = 20.0),
            makeDay("2026-03-28T00:00:00Z", temp = -3.0, precip = 40.0),
            makeDay("2026-03-30T00:00:00Z", temp = -3.0, precip = 40.0),
            makeDay("2026-03-31T00:00:00Z", temp = -3.0, precip = 40.0),
            makeDay("2026-04-01T00:00:00Z", temp = 10.0, precip = 30.0),
        )

        val result = repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
        assertEquals(DangerLevelType.VERY_HIGH, result.dangerLevel.type)
    }

    // Missing data

    @Test
    fun oneObservation_shouldGiveLow() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = -5.0, precip = 15.0)
        )

        val result = repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
        assertEquals(DangerLevelType.LOW, result.dangerLevel.type)
    }

    @Test
    fun missingTemperatureOnAllDays_shouldGiveLow() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = null, precip = 10.0),
            makeDay("2026-03-30T00:00:00Z", temp = null, precip = 10.0),
            makeDay("2026-03-29T00:00:00Z", temp = null, precip = 10.0),
            makeDay("2026-03-28T00:00:00Z", temp = null, precip = 10.0),
            makeDay("2026-03-27T00:00:00Z", temp = null, precip = 10.0),
        )

        val result = repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
        assertNotNull(result.dangerLevel)
        assertEquals(DangerLevelType.LOW, result.dangerLevel.type)
    }

    @Test
    fun missingPrecipitationData_shouldGiveLow() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = -5.0, precip = null),
            makeDay("2026-03-31T00:00:00Z", temp = -5.0, precip = 5.0),
            makeDay("2026-03-30T00:00:00Z", temp = -5.0, precip = null),
        )

        val result = repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
        assertEquals(DangerLevelType.LOW, result.dangerLevel.type)
    }

    // Exception handling

    @Test(expected = NoStationFoundException::class)
    fun noWeatherStationNearby_shouldThrowException() = runTest {
        fakeDataSource.sourceResponse = makeSourceResponse()
        repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
    }

    @Test(expected = NoObservationsException::class)
    fun noObservations_shouldGiveLow() = runTest {
        fakeDataSource.observationResponse = makeObsResponse()
        repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
    }

    @Test(expected = IOException::class)
    fun networkFailure_shouldThrowIOException() = runTest {
        fakeDataSource.shouldThrow = true
        repository.getSlabAvalancheRiskForLocation(
            Coordinate(59.9, 10.7),
        )
    }
}
