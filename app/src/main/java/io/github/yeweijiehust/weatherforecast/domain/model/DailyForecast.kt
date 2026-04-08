package io.github.yeweijiehust.weatherforecast.domain.model

data class DailyForecast(
    val cityId: String,
    val forecastDate: String,
    val tempMax: String,
    val tempMin: String,
    val conditionTextDay: String,
    val conditionIconDay: String,
    val precipitationProbability: String,
    val precipitation: String,
    val windDirectionDay: String,
    val windScaleDay: String,
    val windSpeedDay: String,
    val fetchedAtEpochMillis: Long,
)
