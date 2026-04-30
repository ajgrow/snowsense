package in2000.prosjekt.in2000project.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

fun Typography.scaleAll(factor: Float): Typography{

    fun TextStyle.scaled() = copy(
        fontSize = fontSize * factor,
        lineHeight = lineHeight * factor
    )

    return Typography(
        displayLarge = displayLarge.scaled(),
        displayMedium = displayMedium.scaled(),
        displaySmall = displaySmall.scaled(),

        headlineLarge = headlineLarge.scaled(),
        headlineMedium = headlineMedium.scaled(),
        headlineSmall = headlineSmall.scaled(),

        titleLarge = titleLarge.scaled(),
        titleMedium = titleMedium.scaled(),
        titleSmall = titleSmall.scaled(),

        bodyLarge = bodyLarge.scaled(),
        bodyMedium = bodyMedium.scaled(),
        bodySmall = bodySmall.scaled(),

        labelLarge = labelLarge.scaled(),
        labelMedium = labelMedium.scaled(),
        labelSmall = labelSmall.scaled()
    )
}