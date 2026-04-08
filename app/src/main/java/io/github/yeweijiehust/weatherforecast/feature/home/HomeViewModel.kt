package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveDailyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveCurrentWeatherUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveDefaultCityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveHourlyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshCurrentWeatherUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshDailyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshHourlyForecastUseCase
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeDefaultCityUseCase: ObserveDefaultCityUseCase,
    private val observeCurrentWeatherUseCase: ObserveCurrentWeatherUseCase,
    private val observeHourlyForecastUseCase: ObserveHourlyForecastUseCase,
    private val observeDailyForecastUseCase: ObserveDailyForecastUseCase,
    private val refreshCurrentWeatherUseCase: RefreshCurrentWeatherUseCase,
    private val refreshHourlyForecastUseCase: RefreshHourlyForecastUseCase,
    private val refreshDailyForecastUseCase: RefreshDailyForecastUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentWeatherObservationJob: Job? = null

    init {
        viewModelScope.launch {
            observeDefaultCityUseCase().collectLatest { city ->
                currentWeatherObservationJob?.cancel()
                when (city) {
                    null -> _uiState.value = HomeUiState(state = HomeState.EmptyNoCity)
                    else -> observeCityWeather(city)
                }
            }
        }
    }

    private fun observeCityWeather(city: City) {
        _uiState.value = HomeUiState(state = HomeState.Loading(city))
        currentWeatherObservationJob = viewModelScope.launch {
            combine(
                observeCurrentWeatherUseCase(city.id),
                observeHourlyForecastUseCase(city.id),
                observeDailyForecastUseCase(city.id),
            ) { currentWeather, hourlyForecast, dailyForecast ->
                Triple(currentWeather, hourlyForecast, dailyForecast)
            }.collect { (currentWeather, hourlyForecast, dailyForecast) ->
                if (currentWeather != null) {
                    _uiState.value = HomeUiState(
                        state = HomeState.Content(
                            city = city,
                            currentWeather = currentWeather,
                            hourlyForecast = hourlyForecast,
                            dailyForecast = dailyForecast,
                        ),
                    )
                }
            }
        }
        viewModelScope.launch {
            val currentRefresh = refreshCurrentHourlyDailyWeather(city.id)
            if (currentRefresh.isFailure && !hasContentFor(city.id)) {
                _uiState.value = HomeUiState(state = HomeState.ErrorNoCache(city))
            }
        }
    }

    private suspend fun refreshCurrentHourlyDailyWeather(cityId: String): Result<Unit> = coroutineScope {
        val currentRefresh = async { runCatching { refreshCurrentWeatherUseCase(cityId) } }
        val hourlyRefresh = async { runCatching { refreshHourlyForecastUseCase(cityId) } }
        val dailyRefresh = async { runCatching { refreshDailyForecastUseCase(cityId) } }
        val currentResult = currentRefresh.await()
        hourlyRefresh.await()
        dailyRefresh.await()
        currentResult
    }

    private fun hasContentFor(cityId: String): Boolean {
        val state = _uiState.value.state
        return state is HomeState.Content && state.city.id == cityId
    }
}
