package io.github.yeweijiehust.weatherforecast.data.local.mapper

import io.github.yeweijiehust.weatherforecast.data.local.entity.HourlyForecastEntity
import io.github.yeweijiehust.weatherforecast.data.local.model.HourlyForecastLocalModel
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast

fun HourlyForecastEntity.toLocalModel(): HourlyForecastLocalModel {
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

fun HourlyForecastLocalModel.toEntity(): HourlyForecastEntity {
    return HourlyForecastEntity(
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

fun HourlyForecastLocalModel.toDomain(): HourlyForecast {
    return HourlyForecast(
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
    )
}
