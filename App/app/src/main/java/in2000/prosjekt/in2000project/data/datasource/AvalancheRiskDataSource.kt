package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.datasource.dto.GeoJsonResponseDto

/**
 * Data source for avalanche risk data from NVE (Norges vassdrags- og energidirektorat).
 */
interface AvalancheRiskDataSource {
    /**
     * Fetches avalanche risk areas as GeoJSON for a given location.
     *
     * @param lat Latitude of the query point in WGS84.
     * @param lon Longitude of the query point in WGS84.
     * @param buffer Search radius around the query point in meters.
     * @return A [in2000.prosjekt.in2000project.data.datasource.dto.GeoJsonResponseDto] containing intersecting avalanche risk features,
     * or an empty collection if the request fails.
     */
    suspend fun getGeoJson(lat: Double, lon: Double, buffer: Int): GeoJsonResponseDto
}