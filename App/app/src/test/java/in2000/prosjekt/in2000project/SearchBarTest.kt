package in2000.prosjekt.in2000project

import in2000.prosjekt.in2000project.data.datasource.LocationDataSource
import in2000.prosjekt.in2000project.data.repository.LocationRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

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

        locationDataSource = LocationDataSource(client)
        locationRepository = LocationRepository(locationDataSource)
    }

    @After
    fun tearDown() {
        if (::client.isInitialized) client.close()
    }

    @Test
    fun testLocationQueryBuilder() {
        val result = locationDataSource.buildLocationQuery("blindern")
        assertEquals(
            "https://api.kartverket.no/stedsnavn/v1/navn?sok=blindern&fuzzy=true&utkoordsys=4258&treffPerSide=10&side=1&filtrer=navn.representasjonspunkt%2Cnavn.skrivem%C3%A5te%2Cnavn.navneobjekttype",
            result
        )
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
