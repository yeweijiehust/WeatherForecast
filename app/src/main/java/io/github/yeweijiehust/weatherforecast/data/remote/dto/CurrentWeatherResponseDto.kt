package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeatherResponseDto(
    val code: String,
    val now: CurrentWeatherDto? = null,
)

@Serializable
data class CurrentWeatherDto(
    @SerialName("obsTime")
    val observationTime: String,
    @SerialName("temp")
    val temperature: String,
    @SerialName("feelsLike")
    val feelsLike: String,
    @SerialName("text")
    val conditionText: String,
    @SerialName("icon")
    val conditionIcon: String,
    @SerialName("humidity")
    val humidity: String,
    @SerialName("windDir")
    val windDirection: String,
    @SerialName("windScale")
    val windScale: String,
    @SerialName("windSpeed")
    val windSpeed: String,
    @SerialName("precip")
    val precipitation: String,
    @SerialName("pressure")
    val pressure: String,
    @SerialName("vis")
    val visibility: String,
)
