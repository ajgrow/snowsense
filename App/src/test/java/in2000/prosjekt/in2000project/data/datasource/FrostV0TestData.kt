package in2000.prosjekt.in2000project.data.datasource

import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationDto
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationResponseDto
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0ObservationsAtRefTimeDto
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0PointDto
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0SourceDto
import in2000.prosjekt.in2000project.data.datasource.dto.FrostV0SourceResponseDto

/** Shared factory functions for constructing Frost V0 DTOs in tests. */

fun makeObservation(
    elementId: String = "mean(air_temperature P1D)",
    value: Double = 0.0,
    unit: String = "degC",
    timeOffset: String = "PT0H",
    timeResolution: String = "P1D",
) = FrostV0ObservationDto(elementId, value, unit, timeOffset, timeResolution)

fun makeDay(
    refTime: String = "2026-04-01T00:00:00Z",
    temp: Double? = null,
    precip: Double? = null,
    wind: Double? = null,
    snow: Double? = null,
    sourceId: String = "SN18700",
): FrostV0ObservationsAtRefTimeDto {
    val obs = mutableListOf<FrostV0ObservationDto>()
    if (temp != null) obs += makeObservation(
        elementId = "mean(air_temperature P1D)", value = temp,
    )
    if (precip != null) obs += makeObservation(
        elementId = "sum(precipitation_amount P1D)", value = precip, unit = "mm",
    )
    if (wind != null) obs += makeObservation(
        elementId = "mean(wind_speed P1D)", value = wind, unit = "m/s",
    )
    if (snow != null) obs += makeObservation(
        elementId = "max(surface_snow_thickness P1D)", value = snow, unit = "cm",
    )
    return FrostV0ObservationsAtRefTimeDto(
        sourceId = sourceId,
        referenceTime = refTime,
        observations = obs,
    )
}

fun makeObsResponse(
    vararg days: FrostV0ObservationsAtRefTimeDto,
) = FrostV0ObservationResponseDto(
    currentItemCount = days.size,
    totalItemCount = days.size,
    data = days.toList(),
)

fun makeSource(
    id: String = "SN18700",
    name: String = "Blindern",
    shortName: String = "Blindern",
    geometry: FrostV0PointDto = FrostV0PointDto(
        type = "Point",
        coordinates = listOf(59.94, 10.72),
    ),
    distance: Double? = 1.2,
    masl: Double? = 94.0,
    validFrom: String? = "1937-01-01",
    county: String? = "Oslo",
    municipality: String? = "Oslo",
) = FrostV0SourceDto(id, name, shortName, geometry, distance, masl, validFrom, county, municipality)

fun makeSourceResponse(
    vararg sources: FrostV0SourceDto,
) = FrostV0SourceResponseDto(data = sources.toList())
