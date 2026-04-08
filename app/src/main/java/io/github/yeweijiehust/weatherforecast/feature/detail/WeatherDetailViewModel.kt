package io.github.yeweijiehust.weatherforecast.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.core.navigation.WeatherForecastDestination
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetWeatherAlertsUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveSavedCitiesUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class WeatherDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeSavedCitiesUseCase: ObserveSavedCitiesUseCase,
    private val getWeatherAlertsUseCase: GetWeatherAlertsUseCase,
) : ViewModel() {
    private val cityId = savedStateHandle.get<String>(WeatherForecastDestination.CITY_ID_ARG).orEmpty()

    private val _uiState = MutableStateFlow(WeatherDetailUiState())
    val uiState: StateFlow<WeatherDetailUiState> = _uiState.asStateFlow()

    init {
        if (cityId.isBlank()) {
            _uiState.value = WeatherDetailUiState(state = WeatherDetailState.ErrorNoData(cityId = cityId))
        } else {
            viewModelScope.launch {
                observeSavedCitiesUseCase().collectLatest { cities ->
                    val city = cities.firstOrNull { it.id == cityId }
                    _uiState.value = when (city) {
                        null -> WeatherDetailUiState(state = WeatherDetailState.ErrorNoData(cityId = cityId))
                        else -> {
                            val alertState = runCatching {
                                getWeatherAlertsUseCase(
                                    latitude = city.lat,
                                    longitude = city.lon,
                                )
                            }.getOrElse {
                                _uiState.value = WeatherDetailUiState(
                                    state = WeatherDetailState.PartialContent(
                                        city = city,
                                        alerts = emptyList(),
                                        unavailableSections = setOf(WeatherDetailSection.Alerts),
                                    ),
                                )
                                return@collectLatest
                            }

                            when (alertState) {
                                is WeatherAlertFetchResult.Available -> {
                                    WeatherDetailUiState(
                                        state = WeatherDetailState.Content(
                                            city = city,
                                            alerts = alertState.alerts,
                                        ),
                                    )
                                }

                                WeatherAlertFetchResult.Empty -> {
                                    WeatherDetailUiState(
                                        state = WeatherDetailState.Content(
                                            city = city,
                                            alerts = emptyList(),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
