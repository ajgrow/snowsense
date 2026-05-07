package in2000.prosjekt.in2000project.data.datasource

import java.net.URLEncoder

private const val BASE_URL = "https://api.kartverket.no/stedsnavn/v1/navn"
private const val RESULTS_PER_PAGE = 10
private const val FILTER = "navn.representasjonspunkt,navn.skrivemåte,navn.navneobjekttype"

fun buildLocationQuery(searchQuery: String): String {
    val encodedQuery = URLEncoder.encode(searchQuery, "UTF-8")
    val encodedFilter = URLEncoder.encode(FILTER, "UTF-8")
    return "$BASE_URL?sok=$encodedQuery" +
            "&fuzzy=true" +
            "&utkoordsys=4258" +
            "&treffPerSide=$RESULTS_PER_PAGE" +
            "&side=1" +
            "&filtrer=$encodedFilter"
}