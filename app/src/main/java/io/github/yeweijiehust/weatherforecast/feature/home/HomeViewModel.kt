package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
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

    private var snapshotObservationJob: Job? = null
    private var refreshJob: Job? = null
    private var activeCity: City? = null
    private var latestSnapshot: HomeSnapshot? = null
    private var isRefreshing = false
    private var isStaleCache = false

    init {
        viewModelScope.launch {
            observeDefaultCityUseCase().collectLatest { city ->
                snapshotObservationJob?.cancel()
                refreshJob?.cancel()
                latestSnapshot = null
                activeCity = city
                isRefreshing = false
                isStaleCache = false
                when (city) {
                    null -> _uiState.value = HomeUiState(state = HomeState.EmptyNoCity)
                    else -> observeCityData(city)
                }
            }
        }
    }

    fun onPullToRefresh() {
        val city = activeCity ?: return
        triggerRefresh(city)
    }

    private fun observeCityData(city: City) {
        _uiState.value = HomeUiState(state = HomeState.Loading(city))
        snapshotObservationJob = viewModelScope.launch {
            combine(
                observeCurrentWeatherUseCase(city.id),
                observeHourlyForecastUseCase(city.id),
                observeDailyForecastUseCase(city.id),
            ) { currentWeather, hourlyForecast, dailyForecast ->
                Triple(currentWeather, hourlyForecast, dailyForecast)
            }.collect { (currentWeather, hourlyForecast, dailyForecast) ->
                if (currentWeather != null) {
                    val snapshot = HomeSnapshot(
                        city = city,
                        currentWeather = currentWeather,
                        hourlyForecast = hourlyForecast,
                        dailyForecast = dailyForecast,
                        lastUpdatedEpochMillis = latestUpdatedAt(
                            currentWeather = currentWeather,
                            hourlyForecast = hourlyForecast,
                            dailyForecast = dailyForecast,
                        ),
                    )
                    latestSnapshot = snapshot
                    _uiState.value = HomeUiState(
                        state = when {
                            isRefreshing -> HomeState.Refreshing(snapshot)
                            isStaleCache -> HomeState.ContentWithStaleCache(snapshot)
                            else -> HomeState.Content(snapshot)
                        },
                    )
                }
            }
        }
        triggerRefresh(city)
    }

    private fun triggerRefresh(city: City) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            latestSnapshot?.let { snapshot ->
                isRefreshing = true
                _uiState.value = HomeUiState(
                    state = HomeState.Refreshing(snapshot),
                )
            }

            val currentRefresh = refreshCurrentHourlyDailyWeather(city.id)
            isRefreshing = false
            if (currentRefresh.isSuccess) {
                isStaleCache = false
                latestSnapshot?.let { snapshot ->
                    _uiState.value = HomeUiState(
                        state = HomeState.Content(snapshot),
                    )
                }
            } else if (latestSnapshotFor(city.id) != null) {
                isStaleCache = true
                _uiState.value = HomeUiState(
                    state = HomeState.ContentWithStaleCache(
                        snapshot = latestSnapshotFor(city.id)!!,
                    ),
                )
            } else {
                _uiState.value = HomeUiState(state = HomeState.ErrorNoCache(city))
            }
        }
    }

    private fun latestSnapshotFor(cityId: String): HomeSnapshot? {
        val snapshot = latestSnapshot
        return if (snapshot?.city?.id == cityId) snapshot else null
    }

    private fun latestUpdatedAt(
        currentWeather: CurrentWeather,
        hourlyForecast: List<HourlyForecast>,
        dailyForecast: List<DailyForecast>,
    ): Long {
        return buildList {
            add(currentWeather.fetchedAtEpochMillis)
            addAll(hourlyForecast.map { it.fetchedAtEpochMillis })
            addAll(dailyForecast.map { it.fetchedAtEpochMillis })
        }.maxOrNull() ?: currentWeather.fetchedAtEpochMillis
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
}
