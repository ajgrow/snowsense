package in2000.prosjekt.in2000project.data.model.enums

import androidx.annotation.StringRes
import in2000.prosjekt.in2000project.R

/** European Avalanche Danger Scale levels. */
enum class DangerLevel(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    LOW(R.string.danger_level_1_title, R.string.danger_level_1_description),
    MODERATE(R.string.danger_level_2_title, R.string.danger_level_2_description),
    CONSIDERABLE(R.string.danger_level_3_title, R.string.danger_level_3_description),
    HIGH(R.string.danger_level_4_title, R.string.danger_level_4_description),
    VERY_HIGH(R.string.danger_level_5_title, R.string.danger_level_5_description)
}