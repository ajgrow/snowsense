package in2000.prosjekt.in2000project

import in2000.prosjekt.in2000project.data.datasource.KtorLocationDataSource
import in2000.prosjekt.in2000project.data.datasource.LocationDataSource
import in2000.prosjekt.in2000project.data.repository.KartverketLocationRepository
import in2000.prosjekt.in2000project.domain.repository.LocationRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class SearchBarTest {

    private lateinit var client: HttpClient
    private lateinit var locationDataSource: LocationDataSource
    private lateinit var locationRepository: LocationRepository

    @Before
    fun setUp() {

        client = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        locationDataSource = KtorLocationDataSource(client)
        locationRepository = KartverketLocationRepository(locationDataSource)
    }

    @After
    fun tearDown() {
        if (::client.isInitialized) client.close()
    }

    @Test
    fun testLocationDataSource() {
        runBlocking {
            val result = locationDataSource.getNamesResponse(
                "blindern"
            )
            val name = result?.first()
            assertNotNull(name)
            assertEquals("Blindern", name?.skrivemåte)
            assertEquals(59.94063, name?.representasjonspunkt?.nord ?: 0.0, 0.0)
            assertEquals(10.72307, name?.representasjonspunkt?.øst ?: 0.0, 0.0)
        }
    }

    @Test
    fun testLocationRepository() {
        runBlocking {
            val result = locationRepository.getNamedLocations("blindern")

            val location = result.firstOrNull()
            assertNotNull(location)
            assertEquals("Blindern", location?.name)
            assertEquals(59.94063, location?.coordinate?.latitude ?: 0.0, 0.0)
            assertEquals(10.72307, location?.coordinate?.longitude ?: 0.0, 0.0)
        }
    }
}
