package in2000.prosjekt.in2000project.domain.model

import java.time.LocalDate

/**
 * Represents a daily weather record.
 *
 * @property date The date of the record.
 * @property tempC The temperature in Celsius.
 * @property precipMm The precipitation in millimeters.
 */
data class DailyWeatherRecord(
    val date: LocalDate,
    val tempC: Double?,
    val precipMm: Double?,
)