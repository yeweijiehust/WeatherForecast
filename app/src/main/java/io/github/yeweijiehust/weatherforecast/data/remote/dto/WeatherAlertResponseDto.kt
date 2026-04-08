package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherAlertResponseDto(
    @SerialName("code")
    val code: String,
    @SerialName("warning")
    val warning: List<WeatherAlertDto> = emptyList(),
)

@Serializable
data class WeatherAlertDto(
    @SerialName("id")
    val id: String,
    @SerialName("sender")
    val sender: String = "",
    @SerialName("pubTime")
    val publishTime: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("startTime")
    val startTime: String = "",
    @SerialName("endTime")
    val endTime: String = "",
    @SerialName("status")
    val status: String = "",
    @SerialName("severity")
    val severity: String = "",
    @SerialName("color")
    val severityColor: String = "",
    @SerialName("type")
    val type: String = "",
    @SerialName("typeName")
    val typeName: String = "",
    @SerialName("text")
    val text: String = "",
)
