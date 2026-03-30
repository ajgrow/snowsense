package in2000.prosjekt.in2000project

import in2000.prosjekt.in2000project.data.datasource.LocationDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class SearchBarTest {
    @Test
    fun testLocationAPI() {
        val dataSource = LocationDataSource()
        runBlocking {
            val result = dataSource.fetchLocations("blindern")
            assertTrue(result.isNotEmpty())
        }
    }

    @Test
    fun testLocationRepository() {

    }

    @Test
    fun testLocationSearchResults() {

    }
}