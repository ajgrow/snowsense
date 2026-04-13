package in2000.prosjekt.in2000project.data.model


enum class DangerLevelType(val level: Int, val label: String) {
    UNLIKELY(1, "Unlikely"),
    LOW(2, "Low"),
    MODERATE(3, "Moderate"),
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
