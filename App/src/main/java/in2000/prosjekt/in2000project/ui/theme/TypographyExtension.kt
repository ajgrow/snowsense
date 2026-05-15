package in2000.prosjekt.in2000project.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

fun Typography.scaleAll(factor: Float): Typography {

    fun TextStyle.scaled() = copy(
        fontSize = fontSize * factor,
        lineHeight = lineHeight * factor
    )

    fun TextStyle.scaledLess() = copy(
        fontSize = fontSize * (1f + (factor - 1f) * 0.5f),
        lineHeight = lineHeight * (1f + (factor - 1f) * 0.5f),
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

        labelLarge = labelLarge.scaledLess(),
        labelMedium = labelMedium.scaledLess(),
        labelSmall = labelSmall.scaledLess()
    )
}