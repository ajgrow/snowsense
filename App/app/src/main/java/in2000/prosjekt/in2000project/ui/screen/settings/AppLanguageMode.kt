package in2000.prosjekt.in2000project.ui.screen.settings

import java.util.Locale.forLanguageTag

/**
 * Enum class representing the different language modes.
 *
 * @property ENGLISH The English language mode.
 * @property NORWEGIAN The Norwegian language mode.
 *
 * @param tag The language tag for the mode.
 */
enum class AppLanguageMode(val tag: String) {
    ENGLISH("en"),
    NORWEGIAN("nb");

    companion object {
        fun fromTag(tag: String) = entries.find { it.tag == forLanguageTag(tag).language }
            ?: ENGLISH
    }
}