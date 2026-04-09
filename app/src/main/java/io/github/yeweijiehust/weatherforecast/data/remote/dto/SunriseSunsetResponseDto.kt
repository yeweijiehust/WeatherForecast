package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunsetResponseDto(
    @SerialName("code")
    val code: String,
    @SerialName("updateTime")
    val updateTime: String? = null,
    @SerialName("sunrise")
    val sunrise: String? = null,
    @SerialName("sunset")
    val sunset: String? = null,
)
