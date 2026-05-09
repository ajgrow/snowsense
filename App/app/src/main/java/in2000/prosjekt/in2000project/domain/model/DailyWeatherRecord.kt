package in2000.prosjekt.in2000project.domain.model

import java.time.LocalDate

data class DailyWeatherRecord(
    val date: LocalDate,
    val tempC: Double?,
    val precipMm: Double?,
)