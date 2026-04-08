package io.github.yeweijiehust.weatherforecast.feature.detail

import io.github.yeweijiehust.weatherforecast.domain.model.AirQuality
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlert

data class WeatherDetailUiState(
    val state: WeatherDetailState = WeatherDetailState.Loading,
)

sealed interface WeatherDetailState {
    data object Loading : WeatherDetailState

    data class Content(
        val city: City,
        val alerts: List<WeatherAlert> = emptyList(),
        val airQuality: AirQuality? = null,
        val isAirQualityUnsupported: Boolean = false,
    ) : WeatherDetailState

    data class PartialContent(
        val city: City,
        val alerts: List<WeatherAlert> = emptyList(),
        val airQuality: AirQuality? = null,
        val isAirQualityUnsupported: Boolean = false,
        val unavailableSections: Set<WeatherDetailSection>,
    ) : WeatherDetailState

    data class ErrorNoData(
        val cityId: String,
    ) : WeatherDetailState
}

enum class WeatherDetailSection {
    Alerts,
    AirQuality,
    MinutePrecipitation,
    Astronomy,
    Indices,
}
