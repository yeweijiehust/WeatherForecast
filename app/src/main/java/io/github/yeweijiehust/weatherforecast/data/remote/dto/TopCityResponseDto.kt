package io.github.yeweijiehust.weatherforecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopCityResponseDto(
    @SerialName("code")
    val code: String,
    @SerialName("topCityList")
    val topCityList: List<CityDto> = emptyList(),
)
