package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.ui.UiText
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetAirQualityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetWeatherAlertsUseCase
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val getWeatherAlertsUseCase: GetWeatherAlertsUseCase,
    private val getAirQualityUseCase: GetAirQualityUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    private var snapshotObservationJob: Job? = null
    private var refreshJob: Job? = null
    private var activeCity: City? = null
    private var latestSnapshot: HomeSnapshot? = null
    private var latestSecondarySummary = HomeSecondarySummary()
    private var isRefreshing = false
    private var isStaleCache = false

    init {
        viewModelScope.launch {
            observeDefaultCityUseCase().collectLatest { city ->
                snapshotObservationJob?.cancel()
                refreshJob?.cancel()
                latestSnapshot = null
                activeCity = city
                latestSecondarySummary = HomeSecondarySummary()
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
        triggerRefresh(
            city = city,
            forceRefresh = true,
        )
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
                        secondarySummary = latestSecondarySummary,
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
        triggerRefresh(
            city = city,
            forceRefresh = false,
        )
    }

    private fun triggerRefresh(
        city: City,
        forceRefresh: Boolean,
    ) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            val snapshotBeforeRefresh = latestSnapshotFor(city.id)
            val hadHourlyCache = snapshotBeforeRefresh?.hourlyForecast?.isNotEmpty() == true
            val hadDailyCache = snapshotBeforeRefresh?.dailyForecast?.isNotEmpty() == true

            snapshotBeforeRefresh?.let { snapshot ->
                isRefreshing = true
                _uiState.value = HomeUiState(
                    state = HomeState.Refreshing(snapshot),
                )
            }

            val refreshRunOutcome = refreshCurrentHourlyDailyAndSecondaryWeather(
                city = city,
                forceRefresh = forceRefresh,
            )
            val refreshOutcome = refreshRunOutcome.refreshOutcome
            latestSecondarySummary = refreshRunOutcome.secondarySummary
            updateSnapshotWithSecondarySummary(city.id)
            isRefreshing = false
            when {
                !refreshOutcome.currentSuccess && latestSnapshotFor(city.id) != null -> {
                    isStaleCache = true
                    _uiState.value = HomeUiState(
                        state = HomeState.ContentWithStaleCache(
                            snapshot = latestSnapshotFor(city.id)!!,
                        ),
                    )
                    emitRefreshFailedWithStaleCacheMessage()
                }
                !refreshOutcome.currentSuccess -> {
                    _uiState.value = HomeUiState(state = HomeState.ErrorNoCache(city))
                    emitRefreshFailedMessage()
                }
                refreshOutcome.hasForecastFailureWithCachedData(
                    hadHourlyCache = hadHourlyCache,
                    hadDailyCache = hadDailyCache,
                ) && latestSnapshotFor(city.id) != null -> {
                    isStaleCache = true
                    _uiState.value = HomeUiState(
                        state = HomeState.ContentWithStaleCache(
                            snapshot = latestSnapshotFor(city.id)!!,
                        ),
                    )
                    emitRefreshFailedWithStaleCacheMessage()
                }
                else -> {
                    isStaleCache = false
                    latestSnapshot?.let { snapshot ->
                        _uiState.value = HomeUiState(
                            state = HomeState.Content(snapshot),
                        )
                    }
                    if (!refreshOutcome.isAllSuccess) {
                        emitRefreshFailedMessage()
                    }
                }
            }
        }
    }

    private fun updateSnapshotWithSecondarySummary(cityId: String) {
        val snapshot = latestSnapshotFor(cityId) ?: return
        val updatedSnapshot = snapshot.copy(
            secondarySummary = latestSecondarySummary,
        )
        latestSnapshot = updatedSnapshot
        val state = _uiState.value.state
        _uiState.value = HomeUiState(
            state = when (state) {
                is HomeState.Content -> HomeState.Content(updatedSnapshot)
                is HomeState.Refreshing -> HomeState.Refreshing(updatedSnapshot)
                is HomeState.ContentWithStaleCache -> HomeState.ContentWithStaleCache(updatedSnapshot)
                else -> state
            },
        )
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

    private suspend fun refreshCurrentHourlyDailyAndSecondaryWeather(
        city: City,
        forceRefresh: Boolean,
    ): RefreshRunOutcome = coroutineScope {
        val currentRefresh = async {
            runCatching {
                refreshCurrentWeatherUseCase(
                    cityId = city.id,
                    forceRefresh = forceRefresh,
                )
            }
        }
        val hourlyRefresh = async {
            runCatching {
                refreshHourlyForecastUseCase(
                    cityId = city.id,
                    forceRefresh = forceRefresh,
                )
            }
        }
        val dailyRefresh = async {
            runCatching {
                refreshDailyForecastUseCase(
                    cityId = city.id,
                    forceRefresh = forceRefresh,
                )
            }
        }
        val alertsRefresh = async {
            runCatching {
                getWeatherAlertsUseCase(
                    latitude = city.lat,
                    longitude = city.lon,
                    forceRefresh = forceRefresh,
                )
            }
        }
        val airQualityRefresh = async {
            runCatching {
                getAirQualityUseCase(
                    latitude = city.lat,
                    longitude = city.lon,
                    forceRefresh = forceRefresh,
                )
            }
        }
        RefreshRunOutcome(
            refreshOutcome = RefreshOutcome(
                currentSuccess = currentRefresh.await().isSuccess,
                hourlySuccess = hourlyRefresh.await().isSuccess,
                dailySuccess = dailyRefresh.await().isSuccess,
            ),
            secondarySummary = HomeSecondarySummary(
                alerts = alertsRefresh.await().toHomeAlertsSummary(),
                airQuality = airQualityRefresh.await().toHomeAirQualitySummary(),
            ),
        )
    }

    private data class RefreshRunOutcome(
        val refreshOutcome: RefreshOutcome,
        val secondarySummary: HomeSecondarySummary,
    )

    private data class RefreshOutcome(
        val currentSuccess: Boolean,
        val hourlySuccess: Boolean,
        val dailySuccess: Boolean,
    ) {
        fun hasForecastFailureWithCachedData(
            hadHourlyCache: Boolean,
            hadDailyCache: Boolean,
        ): Boolean {
            return (!hourlySuccess && hadHourlyCache) || (!dailySuccess && hadDailyCache)
        }

        val isAllSuccess: Boolean
            get() = currentSuccess && hourlySuccess && dailySuccess
    }

    private fun Result<WeatherAlertFetchResult>.toHomeAlertsSummary(): HomeAlertsSummary {
        if (isFailure) {
            return HomeAlertsSummary(
                activeAlertCount = null,
                isUnavailable = true,
            )
        }
        return when (val result = getOrThrow()) {
            is WeatherAlertFetchResult.Available -> {
                HomeAlertsSummary(
                    activeAlertCount = result.alerts.size,
                    isUnavailable = false,
                )
            }

            WeatherAlertFetchResult.Empty -> {
                HomeAlertsSummary(
                    activeAlertCount = 0,
                    isUnavailable = false,
                )
            }
        }
    }

    private fun Result<AirQualityFetchResult>.toHomeAirQualitySummary(): HomeAirQualitySummary {
        if (isFailure) {
            return HomeAirQualitySummary(
                aqi = null,
                category = null,
                isUnsupportedRegion = false,
                isUnavailable = true,
            )
        }
        return when (val result = getOrThrow()) {
            is AirQualityFetchResult.Available -> {
                HomeAirQualitySummary(
                    aqi = result.airQuality.aqi,
                    category = result.airQuality.category,
                    isUnsupportedRegion = false,
                    isUnavailable = false,
                )
            }

            AirQualityFetchResult.UnsupportedRegion -> {
                HomeAirQualitySummary(
                    aqi = null,
                    category = null,
                    isUnsupportedRegion = true,
                    isUnavailable = false,
                )
            }

            is AirQualityFetchResult.Failure -> {
                HomeAirQualitySummary(
                    aqi = null,
                    category = null,
                    isUnsupportedRegion = false,
                    isUnavailable = true,
                )
            }
        }
    }

    private suspend fun emitRefreshFailedMessage() {
        _events.emit(
            HomeEvent.ShowMessage(
                message = UiText.StringResource(R.string.snackbar_refresh_failed),
                action = HomeEventAction.RetryRefresh,
            ),
        )
    }

    private suspend fun emitRefreshFailedWithStaleCacheMessage() {
        _events.emit(
            HomeEvent.ShowMessage(
                message = UiText.StringResource(R.string.snackbar_stale_cache_shown),
                action = HomeEventAction.RetryRefresh,
            ),
        )
    }
}
