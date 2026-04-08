package io.github.yeweijiehust.weatherforecast.data.remote.mapper

import io.github.yeweijiehust.weatherforecast.data.local.model.HourlyForecastLocalModel
import io.github.yeweijiehust.weatherforecast.data.remote.dto.HourlyForecastDto

fun HourlyForecastDto.toLocalModel(
    cityId: String,
    fetchedAtEpochMillis: Long,
    language: String,
    unitSystem: String,
): HourlyForecastLocalModel {
    return HourlyForecastLocalModel(
        cityId = cityId,
        forecastTime = forecastTime,
        temperature = temperature,
        conditionText = conditionText,
        conditionIcon = conditionIcon,
        precipitationProbability = precipitationProbability,
        precipitation = precipitation,
        windDirection = windDirection,
        windScale = windScale,
        windSpeed = windSpeed,
        fetchedAtEpochMillis = fetchedAtEpochMillis,
        language = language,
        unitSystem = unitSystem,
    )
}
