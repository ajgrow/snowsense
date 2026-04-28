package in2000.prosjekt.in2000project.data.model.enums

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.ui.theme.DangerColorLevel1
import in2000.prosjekt.in2000project.ui.theme.DangerColorLevel2
import in2000.prosjekt.in2000project.ui.theme.DangerColorLevel3
import in2000.prosjekt.in2000project.ui.theme.DangerColorLevel4
import in2000.prosjekt.in2000project.ui.theme.DangerColorLevel5

/** European Avalanche Danger Scale levels. */
enum class DangerLevel(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val color: Color
) {
    LOW(R.string.danger_level_1_title, R.string.danger_level_1_description, DangerColorLevel1),
    MODERATE(R.string.danger_level_2_title, R.string.danger_level_2_description, DangerColorLevel2),
    CONSIDERABLE(R.string.danger_level_3_title, R.string.danger_level_3_description,
        DangerColorLevel3
    ),
    HIGH(R.string.danger_level_4_title, R.string.danger_level_4_description, DangerColorLevel4),
    VERY_HIGH(R.string.danger_level_5_title, R.string.danger_level_5_description, DangerColorLevel5)
}