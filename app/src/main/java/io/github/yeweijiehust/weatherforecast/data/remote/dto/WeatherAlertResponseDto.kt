package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherAlertResponseDto(
    @SerialName("metadata")
    val metadata: WeatherAlertMetadataDto,
    @SerialName("alerts")
    val alerts: List<WeatherAlertDto> = emptyList(),
)

@Serializable
data class WeatherAlertMetadataDto(
    @SerialName("tag")
    val tag: String? = null,
    @SerialName("zeroResult")
    val zeroResult: Boolean? = null,
)

@Serializable
data class WeatherAlertDto(
    @SerialName("id")
    val id: String,
    @SerialName("senderName")
    val senderName: String? = null,
    @SerialName("issuedTime")
    val issuedTime: String? = null,
    @SerialName("messageType")
    val messageType: WeatherAlertMessageTypeDto? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("effectiveTime")
    val effectiveTime: String? = null,
    @SerialName("onsetTime")
    val onsetTime: String? = null,
    @SerialName("expireTime")
    val expireTime: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("severity")
    val severity: String? = null,
    @SerialName("certainty")
    val certainty: String? = null,
    @SerialName("urgency")
    val urgency: String? = null,
    @SerialName("eventType")
    val eventType: WeatherAlertEventTypeDto? = null,
    @SerialName("color")
    val color: WeatherAlertColorDto? = null,
    @SerialName("headline")
    val headline: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("instruction")
    val instruction: String? = null,
)

@Serializable
data class WeatherAlertMessageTypeDto(
    @SerialName("name")
    val name: String? = null,
    @SerialName("code")
    val code: String? = null,
)

@Serializable
data class WeatherAlertEventTypeDto(
    @SerialName("name")
    val name: String? = null,
    @SerialName("code")
    val code: String? = null,
)

@Serializable
data class WeatherAlertColorDto(
    @SerialName("code")
    val code: String? = null,
    @SerialName("red")
    val red: Int? = null,
    @SerialName("green")
    val green: Int? = null,
    @SerialName("blue")
    val blue: Int? = null,
    @SerialName("alpha")
    val alpha: Double? = null,
)
