package in2000.prosjekt.in2000project.data.datasource

class LocationQueryBuilder {
    fun buildQuery(searchQuery:String) : String {
        return "https://api.kartverket.no/stedsnavn/v1/navn?" +
                "sok=${searchQuery}" +
                "&fuzzy=true&utkoordsys=4258&treffPerSide=10&side=1&filtrer=navn.representasjonspunkt%2Cnavn.skrivem%C3%A5te"
    }
}