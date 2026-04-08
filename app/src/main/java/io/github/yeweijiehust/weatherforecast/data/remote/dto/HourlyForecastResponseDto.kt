package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HourlyForecastResponseDto(
    val code: String,
    val hourly: List<HourlyForecastDto>? = null,
)

@Serializable
data class HourlyForecastDto(
    @SerialName("fxTime")
    val forecastTime: String,
    @SerialName("temp")
    val temperature: String,
    @SerialName("text")
    val conditionText: String,
    @SerialName("icon")
    val conditionIcon: String,
    @SerialName("pop")
    val precipitationProbability: String,
    @SerialName("precip")
    val precipitation: String,
    @SerialName("windDir")
    val windDirection: String,
    @SerialName("windScale")
    val windScale: String,
    @SerialName("windSpeed")
    val windSpeed: String,
)
