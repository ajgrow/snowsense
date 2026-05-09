package in2000.prosjekt.in2000project.data.integration

import in2000.prosjekt.in2000project.data.datasource.KtorFrostV0DataSource
import in2000.prosjekt.in2000project.domain.model.Coordinate
import in2000.prosjekt.in2000project.domain.model.enums.DangerLevel
import in2000.prosjekt.in2000project.domain.model.enums.ElementType
import in2000.prosjekt.in2000project.data.repository.FrostAvalancheRiskRepository
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
import java.time.MonthDay
import kotlin.test.assertContains
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class FrostApiIntegrationTest {

    companion object {
        val BLINDERN = Coordinate(59.9423, 10.72)
        val FROYSTUL = Coordinate(59.7383, 8.4245)
    }

    private lateinit var client: HttpClient
    private lateinit var climateRepository: FrostClimateRepository
    private lateinit var avalancheRepository: FrostAvalancheRiskRepository

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

        val dataSource = KtorFrostV0DataSource(client)
        climateRepository = FrostClimateRepository(dataSource)
        avalancheRepository = FrostAvalancheRiskRepository(dataSource)
    }

    @After
    fun tearDown() {
        if (::client.isInitialized) client.close()
    }

    @Test
    fun blindern_climateData_smokeTest() = runTest {
        val result = climateRepository.getClimateDataForLocation(
            coordinate = BLINDERN,
            periodStart = MonthDay.of(3, 23),
            periodEnd = MonthDay.of(4, 22),
            yearsBack = 5,
        )

        val stationName = assertNotNull(result.stationName, "Station name was null")

        assertTrue(
            stationName.contains("BLINDERN", ignoreCase = true),
            "Expected station name containing BLINDERN, got: $stationName",
        )

        assertTrue(result.dataPointCount >= 20, "got ${result.dataPointCount} data points")
        assertTrue(result.yearlyAverages.size >= 3)

        val elementTypes = result.yearlyAverages.map { it.type }.toSet()

        assertContains(elementTypes, ElementType.TEMPERATURE_MEAN_P1D)
        assertContains(elementTypes, ElementType.PRECIPITATION_SUM_P1D)
    }

    @Test
    fun crossYearBoundary_decToJan_returnsData() = runTest {
        val result = climateRepository.getClimateDataForLocation(
            coordinate = BLINDERN,
            periodStart = MonthDay.of(12, 1),
            periodEnd = MonthDay.of(1, 31),
            yearsBack = 3,
        )

        assertTrue(result.dataPointCount >= 1)
        assertTrue(result.yearlyAverages.isNotEmpty())
    }

    @Test
    fun singleYear_yearsBackZero_returnsData() = runTest {
        val result = climateRepository.getClimateDataForLocation(
            coordinate = BLINDERN,
            periodStart = MonthDay.of(1, 1),
            periodEnd = MonthDay.of(2, 28),
            yearsBack = 0,
        )

        assertTrue(result.dataPointCount >= 1)
        assertEquals(1, result.yearlyAverages.map { it.year }.toSet().size)
    }

    @Test
    fun froystul_avalancheRisk_smokeTest() = runTest {
        val result = avalancheRepository.getWeatherRecordsForLocation(FROYSTUL)

        assertEquals(FROYSTUL, result.coordinate)
        assertContains(DangerLevel.entries, result.dangerLevel)
    }
}
