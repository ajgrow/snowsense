package in2000.prosjekt.in2000project.domain.repository

/**
 * Repository for avalanche risk data.
 */
interface AvalancheRiskRepository {

    /**
     * Returns whether any avalanche risk areas exist at the given location.
     *
     * @param lat Latitude of the query point in WGS84.
     * @param lon Longitude of the query point in WGS84.
     * @param buffer Search radius around the query point in meters.
     * @return `true` if one or more avalanche risk features were found, `false` otherwise.
     */
    suspend fun getGeoJsonStatus(lat: Double, lon: Double, buffer: Int): Boolean
}