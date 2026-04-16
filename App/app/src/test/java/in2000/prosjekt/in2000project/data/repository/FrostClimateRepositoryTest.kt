package in2000.prosjekt.in2000project.data.repository

import in2000.prosjekt.in2000project.data.datasource.*
import in2000.prosjekt.in2000project.data.datasource.dto.*
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.NoObservationsException
import in2000.prosjekt.in2000project.data.model.NoStationFoundException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.MonthDay

class FrostClimateRepositoryTest {

    private lateinit var fakeDataSource: FakeFrostV0DataSource
    private lateinit var repository: FrostClimateRepository

    @Before
    fun setUp() {
        fakeDataSource = FakeFrostV0DataSource()
        repository = FrostClimateRepository(fakeDataSource)
    }

    // ── Correct delegation to data source ──

    @Test
    fun delegatesCoordinateToDataSource() = runTest {
        val coordinate = Coordinate(61.5, 8.3)
        repository.getClimateDataForLocation(
            coordinate,
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            10,
        )

        assertEquals(coordinate, fakeDataSource.lastCoordinate)
    }

    @Test
    fun delegatesStationIdAndElementsToDataSource() = runTest {
        repository.getClimateDataForLocation(
            Coordinate(59.9, 10.7),
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            10,
        )
        assertEquals("SN18700", fakeDataSource.lastSourceId)
        // Verify all four elements are present in the CSV (order doesn't matter)
        val elements = fakeDataSource.lastElements!!
        assertTrue(
            "Missing temperature",
            elements.contains("mean(air_temperature P1D)")
        )
        assertTrue(
            "Missing precipitation",
            elements.contains("sum(precipitation_amount P1D)")
        )
        assertTrue(
            "Missing wind",
            elements.contains("mean(wind_speed P1D)")
        )
        assertTrue(
            "Missing snow",
            elements.contains("max(surface_snow_thickness P1D)")
        )
    }

    // ── Output mapping ──

    @Test
    fun stationMetadata_mappedCorrectly() = runTest {
        val result = repository.getClimateDataForLocation(
            Coordinate(59.9, 10.7),
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            10,
        )

        assertEquals("Blindern", result.stationName)
        assertEquals(1.2, result.stationDistanceKm!!, 0.001)
        assertEquals(94.0, result.stationElevationMasl!!, 0.001)
    }

    @Test
    fun nullableStationFields_mappedAsNull() = runTest {
        // Real stations sometimes lack elevation or distance data.
        fakeDataSource.sourceResponse = makeSourceResponse(
            makeSource(
                id = "SN99999",
                name = "Mystery Station",
                shortName = "Mystery",
                geometry = FrostV0Point(
                    type = "Point", coordinates = listOf(70.0, 25.0),
                ),
                distance = null,
                masl = null,
                validFrom = null,
                county = null,
                municipality = null,
            )
        )
        val result = repository.getClimateDataForLocation(
            Coordinate(70.0, 25.0),
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            10,
        )

        assertEquals("Mystery Station", result.stationName)
        assertNull(result.stationDistanceKm)
        assertNull(result.stationElevationMasl)
    }

    @Test
    fun dailyAverages_computedCorrectly() = runTest {
        // temp -2,-4 → avg -3; precip 4.5,6.5 → avg 5.5; wind 3,5 → avg 4.
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2024-04-01T00:00:00.000Z", temp = -2.0, precip = 4.5, wind = 3.0),
            makeDay("2025-04-01T00:00:00.000Z", temp = -4.0, precip = 6.5, wind = 5.0),
        )
        val result = repository.getClimateDataForLocation(
            Coordinate(59.9, 10.7),
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            10,
        )

        assertEquals(-3.0, result.avgDailyMeanTemperatureC!!, 0.001)
        assertEquals(5.5, result.avgDailyPrecipitationMm!!, 0.001)
        assertEquals(4.0, result.avgDailyMeanWindSpeedMs!!, 0.001)
    }

    @Test
    fun missingElement_returnsNullAverage() = runTest {
        // No snow depth observations — should map to null, not 0 or NaN.
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2024-04-01T00:00:00.000Z", temp = -2.0, precip = 4.5, wind = 3.0),
        )
        val result = repository.getClimateDataForLocation(
            Coordinate(59.9, 10.7),
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            10,
        )

        assertNull(result.avgDailyMaxSnowDepthCm)
    }

    @Test
    fun periodMetadata_passedThrough() = runTest {
        val start = MonthDay.of(3, 23)
        val end = MonthDay.of(4, 22)

        val result = repository.getClimateDataForLocation(
            Coordinate(59.9, 10.7),
            start,
            end,
            10,
        )

        assertEquals(start, result.periodStart)
        assertEquals(end, result.periodEnd)
        assertEquals(10, result.yearsBack)
    }

    @Test
    fun dataPointCount_countsAllObservations() = runTest {
        // 2 ref times × 3 elements = 6 total observations.
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2024-04-01T00:00:00.000Z", temp = -2.0, precip = 4.5, wind = 3.0),
            makeDay("2025-04-01T00:00:00.000Z", temp = -4.0, precip = 6.5, wind = 5.0),
        )
        val result = repository.getClimateDataForLocation(
            Coordinate(59.9, 10.7),
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            10,
        )

        assertEquals(6, result.dataPointCount)
    }

    // ── Error paths ──

    @Test(expected = NoObservationsException::class)
    fun emptyObservations_throwsNoObservationsException() = runTest {
        fakeDataSource.observationResponse = makeObsResponse()
        repository.getClimateDataForLocation(
            Coordinate(59.9, 10.7),
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            10,
        )
    }

    @Test(expected = NoObservationsException::class)
    fun nonEmptyDataButEmptyObservations_throwsNoObservationsException() = runTest {
        // API can return non-empty data with zero observations inside each entry.
        fakeDataSource.observationResponse = makeObsResponse(
            makeDay("2025-04-01T00:00:00.000Z"),
        )
        repository.getClimateDataForLocation(
            Coordinate(59.9, 10.7),
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            10,
        )
    }

    @Test(expected = NoStationFoundException::class)
    fun emptySourceResponse_throwsNoStationFoundException() = runTest {
        fakeDataSource.sourceResponse = makeSourceResponse()
        repository.getClimateDataForLocation(
            Coordinate(59.9, 10.7),
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            10,
        )
    }

    @Test(expected = IOException::class)
    fun networkError_propagates() = runTest {
        fakeDataSource.shouldThrow = true
        repository.getClimateDataForLocation(
            Coordinate(59.9, 10.7),
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            10,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeYearsBack_throwsIllegalArgument() = runTest {
        repository.getClimateDataForLocation(
            Coordinate(59.9, 10.7),
            MonthDay.of(3, 23),
            MonthDay.of(4, 22),
            -1,
        )
    }
}