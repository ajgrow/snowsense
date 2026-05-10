package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.FakeFrostV0DataSource
import in2000.prosjekt.in2000project.data.datasource.makeDay
import in2000.prosjekt.in2000project.data.datasource.makeObsResponse
import in2000.prosjekt.in2000project.data.datasource.makeSourceResponse
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.data.model.exceptions.NoObservationsException
import in2000.prosjekt.in2000project.data.model.exceptions.NoStationFoundException
import in2000.prosjekt.in2000project.domain.SlabAvalancheRiskAssessor
import in2000.prosjekt.in2000project.domain.model.enums.DangerLevel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class FrostAvalancheRiskRepositoryTest {

    private lateinit var fakeDataSource: FakeFrostV0DataSource
    private lateinit var repository: WeatherRepository
    private lateinit var slabAvalancheRiskAssessor: SlabAvalancheRiskAssessor

    @Before
    fun setUp() {
        fakeDataSource = FakeFrostV0DataSource()
        repository = FrostWeatherRepository(fakeDataSource)
        slabAvalancheRiskAssessor = SlabAvalancheRiskAssessor()
    }

    @Test
    fun getWeatherRecordsForLocation_returnsDailyRecords_forCompleteObservations() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = 0.0, precip = 0.0),
            makeDay("2026-03-31T00:00:00Z", temp = 1.0, precip = 2.0),
            makeDay("2026-03-30T00:00:00Z", temp = -1.0, precip = 5.0),
        )

        val result = repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            currentDate = LocalDate.of(2026, 4, 2),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )

        assertEquals(3, result.size)
        assertEquals(LocalDate.of(2026, 4, 1), result[0].date)
        assertEquals(1.0, result[1].tempC)
        assertEquals(5.0, result[2].precipMm)
    }

    @Test
    fun getWeatherRecordsForLocation_excludesCurrentDateObservations() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-02T00:00:00Z", temp = 2.0, precip = 10.0),
            makeDay("2026-04-01T00:00:00Z", temp = -1.0, precip = 4.0),
            makeDay("2026-03-31T00:00:00Z", temp = -2.0, precip = 3.0),
        )

        val result = repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )
        assertEquals(DangerLevel.LOW, slabAvalancheRiskAssessor.assess(result))
    }

    @Test
    fun onlyWarmingNoSnowpack_shouldGiveLow() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = 10.0, precip = 0.0),
            makeDay("2026-03-31T00:00:00Z", temp = -5.0, precip = 0.0),
            makeDay("2026-03-30T00:00:00Z", temp = -5.0, precip = 0.0),
        )

        val result = repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )
        assertEquals(DangerLevel.LOW, slabAvalancheRiskAssessor.assess(result))
    }

    @Test
    fun heavySnowfallButStableCold_shouldGiveModerateAtMost() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = -5.0, precip = 25.0),
            makeDay("2026-03-31T00:00:00Z", temp = -5.0, precip = 25.0),
            makeDay("2026-03-30T00:00:00Z", temp = -5.0, precip = 25.0),
            makeDay("2026-03-29T00:00:00Z", temp = -5.0, precip = 25.0),
        )

        val result = repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )
        assertEquals(DangerLevel.LOW, slabAvalancheRiskAssessor.assess(result))
    }

    @Test
    fun coldPeriodThenMildWithSomePrecip_shouldGiveHigh() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-03-25T00:00:00Z", temp = -8.0, precip = 8.0),
            makeDay("2026-03-26T00:00:00Z", temp = -8.0, precip = 8.0),
            makeDay("2026-03-27T00:00:00Z", temp = -8.0, precip = 8.0),
            makeDay("2026-03-30T00:00:00Z", temp = -2.0, precip = 12.0),
            makeDay("2026-03-31T00:00:00Z", temp = -2.0, precip = 12.0),
            makeDay("2026-04-01T00:00:00Z", temp = 2.0, precip = 15.0),
        )

        val result = repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )

        assertEquals(DangerLevel.HIGH, slabAvalancheRiskAssessor.assess(result))
    }

    @Test
    fun coldPeriodThenWarmDayWithHeavyPrecip_shouldGiveVeryHigh() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-03-22T00:00:00Z", temp = -10.0, precip = 10.0),
            makeDay("2026-03-23T00:00:00Z", temp = -10.0, precip = 10.0),
            makeDay("2026-03-24T00:00:00Z", temp = -10.0, precip = 10.0),
            makeDay("2026-03-30T00:00:00Z", temp = -3.0, precip = 20.0),
            makeDay("2026-03-31T00:00:00Z", temp = -3.0, precip = 20.0),
            makeDay("2026-04-01T00:00:00Z", temp = 5.0, precip = 15.0),
        )

        val result = repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )
        assertEquals(DangerLevel.VERY_HIGH, slabAvalancheRiskAssessor.assess(result))
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

        val result = repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )
        assertEquals(DangerLevel.VERY_HIGH, slabAvalancheRiskAssessor.assess(result))
    }

    // Missing data

    @Test
    fun oneObservation_shouldGiveLow() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = -5.0, precip = 15.0)
        )

        val result = repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )
        assertEquals(DangerLevel.UNKNOWN, slabAvalancheRiskAssessor.assess(result))
    }

    @Test
    fun missingPrecipitationData_shouldGiveUnknown() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = -5.0, precip = null),
            makeDay("2026-03-31T00:00:00Z", temp = -5.0, precip = 5.0),
            makeDay("2026-03-30T00:00:00Z", temp = -5.0, precip = null),
        )

        val result = repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )
        assertEquals(DangerLevel.UNKNOWN, slabAvalancheRiskAssessor.assess(result))
    }

    // Exception handling
    @Test(expected = NoObservationsException::class)
    fun getWeatherRecordsForLocation_throwsWhenNoValidRecords() = runTest {
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2026-04-01T00:00:00Z", temp = -5.0, precip = null),
            makeDay("2026-03-31T00:00:00Z", temp = null, precip = 5.0),
            makeDay("2026-03-30T00:00:00Z", temp = null, precip = null),
        )

        repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            currentDate = LocalDate.of(2026, 4, 2),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )
    }

    @Test(expected = NoStationFoundException::class)
    fun getWeatherRecordsForLocation_throwsWhenNoStationFound() = runTest {
        fakeDataSource.sourceResponse = makeSourceResponse()

        repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            currentDate = LocalDate.of(2026, 4, 2),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )
    }

    @Test(expected = NoObservationsException::class)
    fun getWeatherRecordsForLocation_throwsWhenObservationsAreEmpty() = runTest {
        fakeDataSource.observationResponse = makeObsResponse()

        repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            currentDate = LocalDate.of(2026, 4, 2),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )
    }

    @Test(expected = IOException::class)
    fun getWeatherRecordsForLocation_throwsOnNetworkFailure() = runTest {
        fakeDataSource.shouldThrow = true

        repository.getWeatherRecordsForLocation(
            Coordinate(59.9, 10.7),
            currentDate = LocalDate.of(2026, 4, 2),
            lookBackDays = SlabAvalancheRiskAssessor.LOOKBACK_DAYS
        )
    }
}