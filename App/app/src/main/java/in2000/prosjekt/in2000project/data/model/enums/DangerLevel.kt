package in2000.prosjekt.in2000project.data.model.enums

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.ui.theme.DangerLevel1
import in2000.prosjekt.in2000project.ui.theme.DangerLevel2
import in2000.prosjekt.in2000project.ui.theme.DangerLevel3
import in2000.prosjekt.in2000project.ui.theme.DangerLevel4
import in2000.prosjekt.in2000project.ui.theme.DangerLevel5

/** European Avalanche Danger Scale levels. */
enum class DangerLevel(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val color: Color
) {
    LOW(R.string.danger_level_1_title, R.string.danger_level_1_description, DangerLevel1),
    MODERATE(R.string.danger_level_2_title, R.string.danger_level_2_description, DangerLevel2),
    CONSIDERABLE(R.string.danger_level_3_title, R.string.danger_level_3_description, DangerLevel3),
    HIGH(R.string.danger_level_4_title, R.string.danger_level_4_description, DangerLevel4),
    VERY_HIGH(R.string.danger_level_5_title, R.string.danger_level_5_description, DangerLevel5)
}