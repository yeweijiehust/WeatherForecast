package io.github.yeweijiehust.weatherforecast.data.local.model

data class DailyForecastLocalModel(
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
    val language: String,
    val unitSystem: String,
)
