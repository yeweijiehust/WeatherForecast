package io.github.yeweijiehust.weatherforecast.data.local.mapper

import io.github.yeweijiehust.weatherforecast.data.local.entity.CurrentWeatherEntity
import io.github.yeweijiehust.weatherforecast.data.local.model.CurrentWeatherLocalModel
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather

fun CurrentWeatherEntity.toLocalModel(): CurrentWeatherLocalModel {
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

fun CurrentWeatherLocalModel.toEntity(): CurrentWeatherEntity {
    return CurrentWeatherEntity(
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

fun CurrentWeatherLocalModel.toDomain(): CurrentWeather {
    return CurrentWeather(
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
    )
}
