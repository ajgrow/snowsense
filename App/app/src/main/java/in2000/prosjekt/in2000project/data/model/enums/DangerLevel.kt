package in2000.prosjekt.in2000project.data.model.enums

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.ui.theme.*

/** European Avalanche Danger Scale levels. */
enum class DangerLevel(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val lightColor: Color,
    val darkColor: Color
) {
    LOW(
        R.string.danger_level_1_title,
        R.string.danger_level_1_description,
        DangerColorLevel1,
        DangerColorLevel1Dark
    ),
    MODERATE(
        R.string.danger_level_2_title,
        R.string.danger_level_2_description,
        DangerColorLevel2,
        DangerColorLevel2Dark
    ),
    CONSIDERABLE(
        R.string.danger_level_3_title,
        R.string.danger_level_3_description,
        DangerColorLevel3,
        DangerColorLevel3Dark
    ),
    HIGH(
        R.string.danger_level_4_title,
        R.string.danger_level_4_description,
        DangerColorLevel4,
        DangerColorLevel4Dark
    ),
    VERY_HIGH(
        R.string.danger_level_5_title,
        R.string.danger_level_5_description,
        DangerColorLevel5,
        DangerColorLevel5Dark
    );

    @Composable
    fun getColor(): Color {
        return if (LocalIsDarkTheme.current) darkColor else lightColor
    }
}