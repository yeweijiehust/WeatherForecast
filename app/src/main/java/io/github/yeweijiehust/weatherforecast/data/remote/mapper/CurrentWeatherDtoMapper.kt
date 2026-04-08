package io.github.yeweijiehust.weatherforecast.data.remote.mapper

import io.github.yeweijiehust.weatherforecast.data.local.model.CurrentWeatherLocalModel
import io.github.yeweijiehust.weatherforecast.data.remote.dto.CurrentWeatherDto

fun CurrentWeatherDto.toLocalModel(
    cityId: String,
    fetchedAtEpochMillis: Long,
    language: String,
    unitSystem: String,
): CurrentWeatherLocalModel {
    return CurrentWeatherLocalModel(
        cityId = cityId,
        observationTime = observationTime,
        temperature = temperature,
        feelsLike = feelsLike,
        conditionText = conditionText,
        conditionIcon = conditionIcon,
        humidity = humidity,
        windDirection = windDirection,
        windScale = windScale,
        windSpeed = windSpeed,
        precipitation = precipitation,
        pressure = pressure,
        visibility = visibility,
        fetchedAtEpochMillis = fetchedAtEpochMillis,
        language = language,
        unitSystem = unitSystem,
    )
}
