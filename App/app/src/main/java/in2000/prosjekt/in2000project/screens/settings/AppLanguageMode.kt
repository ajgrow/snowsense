package in2000.prosjekt.in2000project.screens.settings

import java.util.Locale.forLanguageTag

enum class AppLanguageMode(val tag: String) {
    ENGLISH("en"),
    NORWEGIAN("nb");

    companion object {
        fun fromTag(tag: String)
        = entries.find { it.tag == forLanguageTag(tag).language }
            ?: ENGLISH
    }
}