package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityLookupResponseDto(
    @SerialName("code")
    val code: String,
    @SerialName("location")
    val location: List<CityDto> = emptyList(),
)

@Serializable
data class CityDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("adm1")
    val adm1: String = "",
    @SerialName("adm2")
    val adm2: String = "",
    @SerialName("country")
    val country: String = "",
    @SerialName("lat")
    val lat: String = "",
    @SerialName("lon")
    val lon: String = "",
    @SerialName("tz")
    val tz: String = "",
)
