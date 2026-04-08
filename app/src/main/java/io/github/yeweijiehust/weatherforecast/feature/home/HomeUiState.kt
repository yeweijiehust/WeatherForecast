package io.github.yeweijiehust.weatherforecast.feature.home

import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast

data class HomeUiState(
    val state: HomeState = HomeState.EmptyNoCity,
)

sealed interface HomeState {
    data object EmptyNoCity : HomeState

    data class Loading(
        val city: City,
    ) : HomeState

    data class Content(
        val city: City,
        val currentWeather: CurrentWeather,
        val hourlyForecast: List<HourlyForecast>,
    ) : HomeState

    data class ErrorNoCache(
        val city: City,
    ) : HomeState
}
