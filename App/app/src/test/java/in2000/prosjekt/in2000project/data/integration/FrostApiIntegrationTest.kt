package in2000.prosjekt.in2000project.data.integration

import in2000.prosjekt.in2000project.data.datasource.KtorFrostV0DataSource
import in2000.prosjekt.in2000project.data.model.Coordinate
import in2000.prosjekt.in2000project.data.model.enums.ElementType
import in2000.prosjekt.in2000project.data.repository.FrostClimateRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertTrue
import java.time.MonthDay

/**
 * Tests that FrostClimateRepository gets data from Frost API.
 *
 * Should skip automatically if FROST_CLIENT_ID is not available
 * in local.properties.
 */
class FrostApiIntegrationTest {

    private lateinit var client: HttpClient
    private lateinit var repository: FrostClimateRepository

    @Before
    fun setUp() {
        val clientId = System.getProperty("FROST_CLIENT_ID") ?: ""
        assumeTrue(
            "Skipped: FROST_CLIENT_ID missing from local.properties",
            clientId.isNotBlank()
        )

        client = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = clientId, password = "")
                    }
                    sendWithoutRequest { true }
                }
            }
        }

        repository = FrostClimateRepository(KtorFrostV0DataSource(client))
    }

    @After
    fun tearDown() {
        if (::client.isInitialized) client.close()
    }

    @Test
    fun blindern_smokeTest() = runTest {
        val result = repository.getClimateDataForLocation(
            coordinate = Coordinate(59.9423, 10.72),
            periodStart = MonthDay.of(3, 23),
            periodEnd = MonthDay.of(4, 22),
            yearsBack = 5,
        )

        // Prove real data arrived — only the actual Frost API returns "BLINDERN"
        val stationName = requireNotNull(result.stationName) { "Station name was null" }
        assertTrue(
            "Expected station near Blindern but got '$stationName'",
            stationName.contains("BLINDERN", ignoreCase = true)
        )

        // 5 years × ~30 days × multiple elements → well above 20 data points
        assertTrue(
            "Expected >= 20 data points but got ${result.dataPointCount}",
            result.dataPointCount >= 20
        )

        // At least 3 of 5 requested years should have data
        assertTrue(
            "Expected >= 3 yearly averages but got ${result.yearlyAverages.size}",
            result.yearlyAverages.size >= 3
        )

        val elementTypes = result.yearlyAverages.map { it.type }.toSet()
        assertTrue(
            "Expected temperature data in $elementTypes",
            elementTypes.contains(ElementType.TEMPERATURE_MEAN_P1D)
        )
        assertTrue(
            "Expected precipitation data in $elementTypes",
            elementTypes.contains(ElementType.PRECIPITATION_SUM_P1D)
        )

        // Temperature values should be plausible for Mar–Apr in Oslo
        val temperatures = result.yearlyAverages
            .filter { it.type == ElementType.TEMPERATURE_MEAN_P1D }
        assertTrue("Expected temperature observations", temperatures.isNotEmpty())
        for (point in temperatures) {
            assertTrue(
                "Temperature ${point.avgValue}°C in ${point.year} outside plausible Mar–Apr range [-5, 15]",
                point.avgValue in -5.0..15.0
            )
        }
    }

    @Test
    fun crossYearBoundary_decToJan_returnsData() = runTest {
        val result = repository.getClimateDataForLocation(
            coordinate = Coordinate(59.9423, 10.72),
            periodStart = MonthDay.of(12, 1),
            periodEnd = MonthDay.of(1, 31),
            yearsBack = 3,
        )

        assertTrue(
            "Expected >= 1 data point for cross-year period but got ${result.dataPointCount}",
            result.dataPointCount >= 1
        )
        assertTrue(
            "Expected yearly averages for cross-year period but got ${result.yearlyAverages.size}",
            result.yearlyAverages.isNotEmpty()
        )
    }

    @Test
    fun minimalRepetition_yearsBackOne_returnsData() = runTest {
        val result = repository.getClimateDataForLocation(
            coordinate = Coordinate(59.9423, 10.72),
            periodStart = MonthDay.of(1, 1),
            periodEnd = MonthDay.of(2, 28),
            yearsBack = 1,
        )

        assertTrue(
            "Expected >= 1 data point for 2-year request but got ${result.dataPointCount}",
            result.dataPointCount >= 1
        )

        val years = result.yearlyAverages.map { it.year }.toSet()
        assertTrue(
            "Expected 1–2 years in results but got $years",
            years.size in 1..2
        )
    }
}
