package in2000.prosjekt.in2000project.ui.settings

enum class AppLanguageMode(val tag: String) {
    ENGLISH("en"),
    NORWEGIAN("nb");

    companion object {
        fun fromTag(tag: String)
            = entries.find { it.tag == tag } ?: ENGLISH
    }
}