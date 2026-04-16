package in2000.prosjekt.in2000project.data.model

import org.junit.Assert.*
import org.junit.Test

class CoordinateTest {

    @Test
    fun validCoordinate_createsSuccessfully() {
        val coordinate = Coordinate(59.9, 10.7)
        assertEquals(59.9, coordinate.latitude, 0.0)
        assertEquals(10.7, coordinate.longitude, 0.0)
    }

    @Test
    fun boundaryValues_maxLatAndLon() {
        val coordinate = Coordinate(90.0, 180.0)
        assertEquals(90.0, coordinate.latitude, 0.0)
        assertEquals(180.0, coordinate.longitude, 0.0)
    }

    @Test
    fun boundaryValues_minLatAndLon() {
        val coordinate = Coordinate(-90.0, -180.0)
        assertEquals(-90.0, coordinate.latitude, 0.0)
        assertEquals(-180.0, coordinate.longitude, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun latitudeAboveRange_throws() {
        Coordinate(90.001, 10.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun latitudeBelowRange_throws() {
        Coordinate(-90.001, 10.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun longitudeAboveRange_throws() {
        Coordinate(59.9, 180.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun longitudeBelowRange_throws() {
        Coordinate(59.9, -180.001)
    }
}
