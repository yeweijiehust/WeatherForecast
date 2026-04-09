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
    @SerialName("sunrise")
    val sunrise: String? = null,
    @SerialName("sunset")
    val sunset: String? = null,
    @SerialName("moonrise")
    val moonrise: String? = null,
    @SerialName("moonset")
    val moonset: String? = null,
    @SerialName("moonPhase")
    val moonPhase: String? = null,
    @SerialName("moonPhaseIcon")
    val moonPhaseIcon: String? = null,
    @SerialName("tempMax")
    val tempMax: String,
    @SerialName("tempMin")
    val tempMin: String,
    @SerialName("textDay")
    val conditionTextDay: String,
    @SerialName("iconDay")
    val conditionIconDay: String,
    @SerialName("iconNight")
    val conditionIconNight: String? = null,
    @SerialName("textNight")
    val conditionTextNight: String? = null,
    @SerialName("wind360Day")
    val windDirection360Day: String? = null,
    @SerialName("precip")
    val precipitation: String,
    @SerialName("windDirDay")
    val windDirectionDay: String,
    @SerialName("windScaleDay")
    val windScaleDay: String,
    @SerialName("windSpeedDay")
    val windSpeedDay: String,
    @SerialName("wind360Night")
    val windDirection360Night: String? = null,
    @SerialName("windDirNight")
    val windDirectionNight: String? = null,
    @SerialName("windScaleNight")
    val windScaleNight: String? = null,
    @SerialName("windSpeedNight")
    val windSpeedNight: String? = null,
    @SerialName("humidity")
    val humidity: String? = null,
    @SerialName("pressure")
    val pressure: String? = null,
    @SerialName("vis")
    val visibility: String? = null,
    @SerialName("cloud")
    val cloud: String? = null,
    @SerialName("uvIndex")
    val uvIndex: String? = null,
)
