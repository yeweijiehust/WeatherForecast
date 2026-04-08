package io.github.yeweijiehust.weatherforecast.data.local.mapper

import io.github.yeweijiehust.weatherforecast.data.local.entity.DailyForecastEntity
import io.github.yeweijiehust.weatherforecast.data.local.model.DailyForecastLocalModel
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast

fun DailyForecastEntity.toLocalModel(): DailyForecastLocalModel {
    return DailyForecastLocalModel(
        cityId = cityId,
        forecastDate = forecastDate,
        tempMax = tempMax,
        tempMin = tempMin,
        conditionTextDay = conditionTextDay,
        conditionIconDay = conditionIconDay,
        precipitationProbability = precipitationProbability,
        precipitation = precipitation,
        windDirectionDay = windDirectionDay,
        windScaleDay = windScaleDay,
        windSpeedDay = windSpeedDay,
        fetchedAtEpochMillis = fetchedAtEpochMillis,
        language = language,
        unitSystem = unitSystem,
    )
}

fun DailyForecastLocalModel.toEntity(): DailyForecastEntity {
    return DailyForecastEntity(
        cityId = cityId,
        forecastDate = forecastDate,
        tempMax = tempMax,
        tempMin = tempMin,
        conditionTextDay = conditionTextDay,
        conditionIconDay = conditionIconDay,
        precipitationProbability = precipitationProbability,
        precipitation = precipitation,
        windDirectionDay = windDirectionDay,
        windScaleDay = windScaleDay,
        windSpeedDay = windSpeedDay,
        fetchedAtEpochMillis = fetchedAtEpochMillis,
        language = language,
        unitSystem = unitSystem,
    )
}

fun DailyForecastLocalModel.toDomain(): DailyForecast {
    return DailyForecast(
        cityId = cityId,
        forecastDate = forecastDate,
        tempMax = tempMax,
        tempMin = tempMin,
        conditionTextDay = conditionTextDay,
        conditionIconDay = conditionIconDay,
        precipitationProbability = precipitationProbability,
        precipitation = precipitation,
        windDirectionDay = windDirectionDay,
        windScaleDay = windScaleDay,
        windSpeedDay = windSpeedDay,
        fetchedAtEpochMillis = fetchedAtEpochMillis,
    )
}
