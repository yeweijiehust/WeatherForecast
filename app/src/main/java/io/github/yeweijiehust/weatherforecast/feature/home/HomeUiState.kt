package io.github.yeweijiehust.weatherforecast.feature.home

import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.ui.UiText
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast

data class HomeUiState(
    val state: HomeState = HomeState.Uninitialized,
)

sealed interface HomeState {
    data object Uninitialized : HomeState

    data object EmptyNoCity : HomeState

    data class Loading(
        val city: City,
    ) : HomeState

    data class Content(
        val snapshot: HomeSnapshot,
    ) : HomeState

    data class Refreshing(
        val snapshot: HomeSnapshot,
    ) : HomeState

    data class ContentWithStaleCache(
        val snapshot: HomeSnapshot,
    ) : HomeState

    data class ErrorNoCache(
        val city: City,
    ) : HomeState
}

data class HomeSnapshot(
    val city: City,
    val currentWeather: CurrentWeather,
    val hourlyForecast: List<HourlyForecast>,
    val dailyForecast: List<DailyForecast>,
    val lastUpdatedEpochMillis: Long,
)

sealed interface HomeEvent {
    data class ShowMessage(
        val message: UiText,
        val action: HomeEventAction? = null,
    ) : HomeEvent
}

enum class HomeEventAction(
    val actionLabel: UiText,
) {
    RetryRefresh(
        actionLabel = UiText.StringResource(R.string.action_retry),
    ),
}
