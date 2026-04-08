package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyForecastResponseDto(
    val code: String,
    val daily: List<DailyForecastDto>? = null,
)

@Serializable
data class DailyForecastDto(
    @SerialName("fxDate")
    val forecastDate: String,
    @SerialName("tempMax")
    val tempMax: String,
    @SerialName("tempMin")
    val tempMin: String,
    @SerialName("textDay")
    val conditionTextDay: String,
    @SerialName("iconDay")
    val conditionIconDay: String,
    @SerialName("pop")
    val precipitationProbability: String,
    @SerialName("precip")
    val precipitation: String,
    @SerialName("windDirDay")
    val windDirectionDay: String,
    @SerialName("windScaleDay")
    val windScaleDay: String,
    @SerialName("windSpeedDay")
    val windSpeedDay: String,
)
