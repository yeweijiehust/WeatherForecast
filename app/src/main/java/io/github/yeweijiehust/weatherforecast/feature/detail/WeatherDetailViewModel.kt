package io.github.yeweijiehust.weatherforecast.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.core.navigation.WeatherForecastDestination
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetAirQualityUseCase
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
    private val getAirQualityUseCase: GetAirQualityUseCase,
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
                            val unavailableSections = mutableSetOf<WeatherDetailSection>()

                            val alertState = runCatching {
                                getWeatherAlertsUseCase(
                                    latitude = city.lat,
                                    longitude = city.lon,
                                )
                            }.getOrElse {
                                unavailableSections += WeatherDetailSection.Alerts
                                WeatherAlertFetchResult.Empty
                            }

                            val airQualityState = runCatching {
                                getAirQualityUseCase(
                                    latitude = city.lat,
                                    longitude = city.lon,
                                )
                            }.getOrElse {
                                unavailableSections += WeatherDetailSection.AirQuality
                                AirQualityFetchResult.Failure(reason = AirQualityFailureReason.Unknown)
                            }

                            val alerts = when (alertState) {
                                is WeatherAlertFetchResult.Available -> alertState.alerts
                                WeatherAlertFetchResult.Empty -> emptyList()
                            }
                            val airQuality = when (airQualityState) {
                                is AirQualityFetchResult.Available -> airQualityState.airQuality
                                AirQualityFetchResult.UnsupportedRegion -> null
                                is AirQualityFetchResult.Failure -> {
                                    unavailableSections += WeatherDetailSection.AirQuality
                                    null
                                }
                            }
                            val isAirQualityUnsupported =
                                airQualityState is AirQualityFetchResult.UnsupportedRegion

                            if (unavailableSections.isEmpty()) {
                                WeatherDetailUiState(
                                    state = WeatherDetailState.Content(
                                        city = city,
                                        alerts = alerts,
                                        airQuality = airQuality,
                                        isAirQualityUnsupported = isAirQualityUnsupported,
                                    ),
                                )
                            } else {
                                WeatherDetailUiState(
                                    state = WeatherDetailState.PartialContent(
                                        city = city,
                                        alerts = alerts,
                                        airQuality = airQuality,
                                        isAirQualityUnsupported = isAirQualityUnsupported,
                                        unavailableSections = unavailableSections.toSet(),
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
