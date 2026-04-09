package io.github.yeweijiehust.weatherforecast.data.remote.mapper

import io.github.yeweijiehust.weatherforecast.data.local.model.DailyForecastLocalModel
import io.github.yeweijiehust.weatherforecast.data.remote.dto.DailyForecastDto

fun DailyForecastDto.toLocalModel(
    cityId: String,
    fetchedAtEpochMillis: Long,
    language: String,
    unitSystem: String,
): DailyForecastLocalModel {
    return DailyForecastLocalModel(
        cityId = cityId,
        forecastDate = forecastDate,
        tempMax = tempMax,
        tempMin = tempMin,
        conditionTextDay = conditionTextDay,
        conditionIconDay = conditionIconDay,
        precipitationProbability = "--",
        precipitation = precipitation,
        windDirectionDay = windDirectionDay,
        windScaleDay = windScaleDay,
        windSpeedDay = windSpeedDay,
        fetchedAtEpochMillis = fetchedAtEpochMillis,
        language = language,
        unitSystem = unitSystem,
    )
}
