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

        repository = FrostClimateRepository(
            KtorFrostV0DataSource(client))
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

        val stationName = requireNotNull(result.stationName) {
            "Station name was null" }

        assertTrue(stationName.contains("BLINDERN", ignoreCase = true))

        // Manual tests showed BLINDERN returns at least 20 data points.
        assertTrue(
            "got ${result.dataPointCount} data points",
            result.dataPointCount >= 20)

        assertTrue(result.yearlyAverages.size >= 3)

        val elementTypes = result.yearlyAverages.map { it.type }.toSet()

        // Manual tests showed BLINDERN measures at least these elements.
        assertTrue(elementTypes.contains(ElementType.TEMPERATURE_MEAN_P1D))
        assertTrue(elementTypes.contains(ElementType.PRECIPITATION_SUM_P1D))
    }

    @Test
    fun crossYearBoundary_decToJan_returnsData() = runTest {
        val result = repository.getClimateDataForLocation(
            coordinate = Coordinate(59.9423, 10.72),
            periodStart = MonthDay.of(12, 1),
            periodEnd = MonthDay.of(1, 31),
            yearsBack = 3,
        )

        assertTrue(result.dataPointCount >= 1)
        assertTrue(result.yearlyAverages.isNotEmpty())
    }

    @Test
    fun singleYear_yearsBackZero_returnsData() = runTest {
        val result = repository.getClimateDataForLocation(
            coordinate = Coordinate(59.9423, 10.72),
            periodStart = MonthDay.of(1, 1),
            periodEnd = MonthDay.of(2, 28),
            yearsBack = 0,
        )

        assertTrue(result.dataPointCount >= 1)
        assertTrue(result.yearlyAverages.map { it.year }.toSet().size == 1)
    }
}
