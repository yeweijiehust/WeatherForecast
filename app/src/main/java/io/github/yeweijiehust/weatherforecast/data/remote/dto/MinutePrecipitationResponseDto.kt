package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MinutePrecipitationResponseDto(
    @SerialName("code")
    val code: String,
    @SerialName("summary")
    val summary: String? = null,
    @SerialName("updateTime")
    val updateTime: String? = null,
    @SerialName("minutely")
    val minutely: List<MinutePrecipitationPointDto> = emptyList(),
)

@Serializable
data class MinutePrecipitationPointDto(
    @SerialName("fxTime")
    val forecastTime: String,
    @SerialName("precip")
    val precipitation: String? = null,
    @SerialName("type")
    val type: String? = null,
)
