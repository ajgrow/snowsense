package in2000.prosjekt.in2000project.ui.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import in2000.prosjekt.in2000project.data.model.enums.DangerLevel
import in2000.prosjekt.in2000project.R
import in2000.prosjekt.in2000project.ui.theme.*

/* Since danger level is shown on both the Map and Info screen, this model
provides a single source of truth for the composables that use them
 */
data class DangerLevelUiModel(
    val level: DangerLevel,
    @StringRes val titleRes: Int,
    @StringRes val badgeLabelRes: Int,
    @StringRes val descriptionRes: Int,
    val lightColor: Color,
    val darkColor: Color
) {
    /*Sets the correct color based on the current theme (Dark/Light).*/
    @Composable
    fun getColor(): Color {
        return if (LocalIsDarkTheme.current) darkColor else lightColor
    }
}

/*Mapper extension function to convert a DangerLevel into a UI model.*/
fun DangerLevel.toUiModel(): DangerLevelUiModel {
    return when (this) {
        DangerLevel.UNKNOWN -> DangerLevelUiModel(
            level = this,
            titleRes = R.string.danger_level_0_title,
            badgeLabelRes = R.string.danger_level_0_badge,
            descriptionRes = R.string.danger_level_0_description,
            lightColor = DangerColorLevel0,
            darkColor = DangerColorLevel0Dark
        )
        DangerLevel.LOW -> DangerLevelUiModel(
            level = this,
            titleRes = R.string.danger_level_1_title,
            badgeLabelRes = R.string.danger_level_1_badge,
            descriptionRes = R.string.danger_level_1_description,
            lightColor = DangerColorLevel1,
            darkColor = DangerColorLevel1Dark
        )
        DangerLevel.MODERATE -> DangerLevelUiModel(
            level = this,
            titleRes = R.string.danger_level_2_title,
            badgeLabelRes = R.string.danger_level_2_badge,            descriptionRes = R.string.danger_level_2_description,
            lightColor = DangerColorLevel2,
            darkColor = DangerColorLevel2Dark
        )
        DangerLevel.CONSIDERABLE -> DangerLevelUiModel(
            level = this,
            titleRes = R.string.danger_level_3_title,
            badgeLabelRes = R.string.danger_level_3_badge,
            descriptionRes = R.string.danger_level_3_description,
            lightColor = DangerColorLevel3,
            darkColor = DangerColorLevel3Dark
        )
        DangerLevel.HIGH -> DangerLevelUiModel(
            level = this,
            titleRes = R.string.danger_level_4_title,
            badgeLabelRes = R.string.danger_level_4_badge,
            descriptionRes = R.string.danger_level_4_description,
            lightColor = DangerColorLevel4,
            darkColor = DangerColorLevel4Dark
        )
        DangerLevel.VERY_HIGH -> DangerLevelUiModel(
            level = this,
            titleRes = R.string.danger_level_5_title,
            badgeLabelRes = R.string.danger_level_5_badge,
            descriptionRes = R.string.danger_level_5_description,
            lightColor = DangerColorLevel5,
            darkColor = DangerColorLevel5Dark
        )
    }
}
