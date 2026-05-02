package in2000.prosjekt.in2000project

import in2000.prosjekt.in2000project.data.datasource.LocationDataSource
import in2000.prosjekt.in2000project.data.datasource.LocationQueryBuilder
import in2000.prosjekt.in2000project.data.repository.LocationRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class SearchBarTest {
    @Test
    fun testLocationAPI() {
        val dataSource = LocationDataSource()

        runBlocking {
            val result = dataSource.getNamesResponse(
                "https://api.kartverket.no/stedsnavn/v1/navn?sok=blindern&fuzzy=false&utkoordsys=4258&treffPerSide=10&side=1&filtrer=navn.representasjonspunkt%2Cnavn.skrivem%C3%A5te"
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
        val repository = LocationRepository()

        runBlocking {
            val result = repository.getNamedLocations("https://api.kartverket.no/stedsnavn/v1/navn?sok=blindern&fuzzy=false&utkoordsys=4258&treffPerSide=10&side=1&filtrer=navn.representasjonspunkt%2Cnavn.skrivem%C3%A5te")
            assertTrue(result.isNotEmpty())
        }
    }

    @Test
    fun testLocationQueryBuilder() {
        val queryBuilder = LocationQueryBuilder()
        val result = queryBuilder.buildQuery("blindern")
        assertEquals(
            "https://api.kartverket.no/stedsnavn/v1/navn?sok=blindern&fuzzy=true&utkoordsys=4258&treffPerSide=10&side=1&filtrer=navn.representasjonspunkt%2Cnavn.skrivem%C3%A5te",
            result
        )
    }
}
