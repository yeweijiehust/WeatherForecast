package io.github.yeweijiehust.weatherforecast.feature.search

import io.github.yeweijiehust.weatherforecast.core.ui.UiText
import io.github.yeweijiehust.weatherforecast.domain.model.City

data class CitySearchUiState(
    val query: String = "",
    val resultState: CitySearchResultState = CitySearchResultState.Idle,
    val savedCities: List<City> = emptyList(),
    val topCitySuggestions: List<City> = emptyList(),
)

sealed interface CitySearchResultState {
    data object Idle : CitySearchResultState

    data object Searching : CitySearchResultState

    data class Results(
        val cities: List<City>,
    ) : CitySearchResultState

    data class EmptyResult(
        val query: String,
    ) : CitySearchResultState

    data class Error(
        val query: String,
        val message: UiText,
    ) : CitySearchResultState
}

sealed interface CitySearchEvent {
    data class ShowMessage(
        val message: UiText,
    ) : CitySearchEvent
}
