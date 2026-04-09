package in2000.prosjekt.in2000project.util

fun isInPolygon(lat: Double, lon: Double, coordinates: List<List<List<Double>>>): Boolean{
    val ytreRing = coordinates.firstOrNull() ?: return false
    var isInsidePolygon = false

    for(i in ytreRing.indices){
        var temp_i = 0
        if (i == 0){
            temp_i = ytreRing.size - 1
        } else {
            temp_i = i - 1
        }
        val previ = temp_i
    }
    return isInsidePolygon
}