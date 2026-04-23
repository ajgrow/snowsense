package in2000.prosjekt.in2000project.data.model


enum class DangerLevelType(val level: Int, val label: String) {
    LOW(1, "Low"),
    MODERATE(2, "Moderate"),
    CONSIDERABLE(3, "Considerable"),
    HIGH(4, "High"),
    VERY_HIGH(5, "Very High")
}

data class DangerLevel(
    val type: DangerLevelType,
    val longDescription: String? = null
) {
    val level: Int get() = type.level
    val label: String get() = type.label
}
