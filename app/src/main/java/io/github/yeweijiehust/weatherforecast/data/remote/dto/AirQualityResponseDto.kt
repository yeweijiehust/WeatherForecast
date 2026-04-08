package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AirQualityResponseDto(
    @SerialName("code")
    val code: String,
    @SerialName("now")
    val now: AirQualityCurrentDto? = null,
)

@Serializable
data class AirQualityCurrentDto(
    @SerialName("pubTime")
    val publishTime: String? = null,
    @SerialName("aqi")
    val aqi: String? = null,
    @SerialName("category")
    val category: String? = null,
    @SerialName("primary")
    val primary: String? = null,
    @SerialName("pm2p5")
    val pm2p5: String? = null,
    @SerialName("pm10")
    val pm10: String? = null,
    @SerialName("no2")
    val no2: String? = null,
    @SerialName("so2")
    val so2: String? = null,
    @SerialName("co")
    val co: String? = null,
    @SerialName("o3")
    val o3: String? = null,
)
