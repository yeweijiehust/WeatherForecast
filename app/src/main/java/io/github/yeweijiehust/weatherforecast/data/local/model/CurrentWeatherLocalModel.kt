package io.github.yeweijiehust.weatherforecast.data.local.model

data class CurrentWeatherLocalModel(
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
    val language: String,
    val unitSystem: String,
)
