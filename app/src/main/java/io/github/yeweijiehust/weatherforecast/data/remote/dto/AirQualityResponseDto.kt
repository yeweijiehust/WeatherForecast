package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AirQualityResponseDto(
    @SerialName("metadata")
    val metadata: AirQualityMetadataDto,
    @SerialName("indexes")
    val indexes: List<AirQualityIndexDto> = emptyList(),
    @SerialName("pollutants")
    val pollutants: List<AirQualityPollutantDto> = emptyList(),
)

@Serializable
data class AirQualityMetadataDto(
    @SerialName("tag")
    val tag: String? = null,
    @SerialName("zeroResult")
    val zeroResult: Boolean? = null,
)

@Serializable
data class AirQualityIndexDto(
    @SerialName("aqiDisplay")
    val aqiDisplay: String? = null,
    @SerialName("aqi")
    val aqi: JsonElement? = null,
    @SerialName("category")
    val category: String? = null,
    @SerialName("primaryPollutant")
    val primaryPollutant: AirQualityPrimaryPollutantDto? = null,
)

@Serializable
data class AirQualityPrimaryPollutantDto(
    @SerialName("code")
    val code: String? = null,
    @SerialName("name")
    val name: String? = null,
)

@Serializable
data class AirQualityPollutantDto(
    @SerialName("code")
    val code: String? = null,
    @SerialName("concentration")
    val concentration: AirQualityConcentrationDto? = null,
)

@Serializable
data class AirQualityConcentrationDto(
    @SerialName("value")
    val value: JsonElement? = null,
    @SerialName("unit")
    val unit: String? = null,
)
