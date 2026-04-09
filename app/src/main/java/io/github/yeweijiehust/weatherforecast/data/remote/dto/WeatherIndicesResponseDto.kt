package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherIndicesResponseDto(
    @SerialName("code")
    val code: String,
    @SerialName("updateTime")
    val updateTime: String? = null,
    @SerialName("daily")
    val daily: List<WeatherIndexDto> = emptyList(),
)

@Serializable
data class WeatherIndexDto(
    @SerialName("date")
    val date: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("level")
    val level: String? = null,
    @SerialName("category")
    val category: String? = null,
    @SerialName("text")
    val text: String? = null,
)
