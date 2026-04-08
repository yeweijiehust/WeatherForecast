package io.github.yeweijiehust.weatherforecast.domain.model

data class CurrentWeather(
    val cityId: String,
    val observationTime: String,
    val temperature: String,
    val feelsLike: String,
    val conditionText: String,
    val conditionIcon: String,
    val humidity: String,
    val windDirection: String,
    val windScale: String,
    val windSpeed: String,
    val precipitation: String,
    val pressure: String,
    val visibility: String,
    val fetchedAtEpochMillis: Long,
)
