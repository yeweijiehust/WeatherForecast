package io.github.yeweijiehust.weatherforecast.domain.model

data class HourlyForecast(
    val cityId: String,
    val forecastTime: String,
    val temperature: String,
    val conditionText: String,
    val conditionIcon: String,
    val precipitationProbability: String,
    val precipitation: String,
    val windDirection: String,
    val windScale: String,
    val windSpeed: String,
    val fetchedAtEpochMillis: Long,
)
