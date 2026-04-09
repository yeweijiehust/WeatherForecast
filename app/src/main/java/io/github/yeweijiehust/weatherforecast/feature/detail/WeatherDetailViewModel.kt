package io.github.yeweijiehust.weatherforecast.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.core.navigation.WeatherForecastDestination
import io.github.yeweijiehust.weatherforecast.domain.model.AirQuality
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationTimeline
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunset
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlert
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndices
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFetchResult
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetAirQualityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetMinutePrecipitationUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetSunriseSunsetUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetWeatherAlertsUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetWeatherIndicesUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveDailyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveHourlyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveSavedCitiesUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshDailyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshHourlyForecastUseCase
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@HiltViewModel
class WeatherDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeSavedCitiesUseCase: ObserveSavedCitiesUseCase,
    private val observeHourlyForecastUseCase: ObserveHourlyForecastUseCase,
    private val observeDailyForecastUseCase: ObserveDailyForecastUseCase,
    private val refreshHourlyForecastUseCase: RefreshHourlyForecastUseCase,
    private val refreshDailyForecastUseCase: RefreshDailyForecastUseCase,
    private val getWeatherAlertsUseCase: GetWeatherAlertsUseCase,
    private val getAirQualityUseCase: GetAirQualityUseCase,
    private val getMinutePrecipitationUseCase: GetMinutePrecipitationUseCase,
    private val getSunriseSunsetUseCase: GetSunriseSunsetUseCase,
    private val getWeatherIndicesUseCase: GetWeatherIndicesUseCase,
) : ViewModel() {
    private val cityId = savedStateHandle.get<String>(WeatherForecastDestination.CITY_ID_ARG).orEmpty()

    private val _uiState = MutableStateFlow(WeatherDetailUiState())
    val uiState: StateFlow<WeatherDetailUiState> = _uiState.asStateFlow()

    private var activeCity: City? = null
    private var observeHourlyJob: Job? = null
    private var observeDailyJob: Job? = null

    private var hourlyForecast: List<HourlyForecast>? = null
    private var dailyForecast: List<DailyForecast>? = null
    private var minutePrecipitation: MinutePrecipitationTimeline? = null
    private var isMinutePrecipitationUnsupported = false
    private var sunriseSunset: SunriseSunset? = null
    private var weatherIndices: WeatherIndices? = null
    private var alerts: List<WeatherAlert> = emptyList()
    private var airQuality: AirQuality? = null
    private var isAirQualityUnsupported = false
    private val unavailableSections = linkedSetOf<WeatherDetailSection>()

    init {
        if (cityId.isBlank()) {
            _uiState.value = WeatherDetailUiState(state = WeatherDetailState.ErrorNoData(cityId = cityId))
        } else {
            viewModelScope.launch {
                observeSavedCitiesUseCase().collectLatest { cities ->
                    val city = cities.firstOrNull { it.id == cityId }
                    if (city == null) {
                        _uiState.value = WeatherDetailUiState(
                            state = WeatherDetailState.ErrorNoData(cityId = cityId),
                        )
                        return@collectLatest
                    }
                    initializeForCity(city)
                }
            }
        }
    }

    fun retryHourlySection() {
        val city = activeCity ?: return
        viewModelScope.launch {
            refreshHourlySection(
                cityId = city.id,
                forceRefresh = true,
            )
        }
    }

    fun retryDailySection() {
        val city = activeCity ?: return
        viewModelScope.launch {
            refreshDailySection(
                cityId = city.id,
                forceRefresh = true,
            )
        }
    }

    fun retryAlertsSection() {
        val city = activeCity ?: return
        viewModelScope.launch {
            refreshAlertsSection(
                city = city,
                forceRefresh = true,
            )
        }
    }

    fun retryMinutePrecipitationSection() {
        val city = activeCity ?: return
        viewModelScope.launch {
            refreshMinutePrecipitationSection(
                city = city,
                forceRefresh = true,
            )
        }
    }

    fun retryAstronomySection() {
        val city = activeCity ?: return
        viewModelScope.launch {
            refreshAstronomySection(
                city = city,
                forceRefresh = true,
            )
        }
    }

    fun retryIndicesSection() {
        val city = activeCity ?: return
        viewModelScope.launch {
            refreshIndicesSection(
                city = city,
                forceRefresh = true,
            )
        }
    }

    fun retryAirQualitySection() {
        val city = activeCity ?: return
        viewModelScope.launch {
            refreshAirQualitySection(
                city = city,
                forceRefresh = true,
            )
        }
    }

    private fun initializeForCity(city: City) {
        activeCity = city
        hourlyForecast = null
        dailyForecast = null
        minutePrecipitation = null
        isMinutePrecipitationUnsupported = false
        sunriseSunset = null
        weatherIndices = null
        alerts = emptyList()
        airQuality = null
        isAirQualityUnsupported = false
        unavailableSections.clear()
        _uiState.value = WeatherDetailUiState(state = WeatherDetailState.Loading)
        startForecastObservation(city.id)
        viewModelScope.launch { refreshHourlySection(city.id, forceRefresh = false) }
        viewModelScope.launch { refreshDailySection(city.id, forceRefresh = false) }
        viewModelScope.launch { refreshMinutePrecipitationSection(city, forceRefresh = false) }
        viewModelScope.launch { refreshAstronomySection(city, forceRefresh = false) }
        viewModelScope.launch { refreshIndicesSection(city, forceRefresh = false) }
        viewModelScope.launch { refreshAlertsSection(city, forceRefresh = false) }
        viewModelScope.launch { refreshAirQualitySection(city, forceRefresh = false) }
    }

    private fun startForecastObservation(cityId: String) {
        observeHourlyJob?.cancel()
        observeDailyJob?.cancel()

        observeHourlyJob = viewModelScope.launch {
            observeHourlyForecastUseCase(cityId).collectLatest { forecast ->
                hourlyForecast = forecast
                if (forecast.isNotEmpty()) {
                    unavailableSections.remove(WeatherDetailSection.HourlyForecast)
                }
                emitStateIfReady()
            }
        }
        observeDailyJob = viewModelScope.launch {
            observeDailyForecastUseCase(cityId).collectLatest { forecast ->
                dailyForecast = forecast
                if (forecast.isNotEmpty()) {
                    unavailableSections.remove(WeatherDetailSection.DailyForecast)
                }
                emitStateIfReady()
            }
        }
    }

    private suspend fun refreshHourlySection(
        cityId: String,
        forceRefresh: Boolean,
    ) {
        val result = runCatching {
            refreshHourlyForecastUseCase(
                cityId = cityId,
                forceRefresh = forceRefresh,
            )
        }
        if (result.isFailure && hourlyForecast.orEmpty().isEmpty()) {
            unavailableSections.add(WeatherDetailSection.HourlyForecast)
        } else if (result.isSuccess) {
            unavailableSections.remove(WeatherDetailSection.HourlyForecast)
        }
        emitStateIfReady()
    }

    private suspend fun refreshDailySection(
        cityId: String,
        forceRefresh: Boolean,
    ) {
        val result = runCatching {
            refreshDailyForecastUseCase(
                cityId = cityId,
                forceRefresh = forceRefresh,
            )
        }
        if (result.isFailure && dailyForecast.orEmpty().isEmpty()) {
            unavailableSections.add(WeatherDetailSection.DailyForecast)
        } else if (result.isSuccess) {
            unavailableSections.remove(WeatherDetailSection.DailyForecast)
        }
        emitStateIfReady()
    }

    private suspend fun refreshAlertsSection(
        city: City,
        forceRefresh: Boolean,
    ) {
        val result = runCatching {
            getWeatherAlertsUseCase(
                latitude = city.lat,
                longitude = city.lon,
                forceRefresh = forceRefresh,
            )
        }
        result.onFailure {
            alerts = emptyList()
            unavailableSections.add(WeatherDetailSection.Alerts)
            emitStateIfReady()
            return
        }
        when (val state = result.getOrThrow()) {
            is WeatherAlertFetchResult.Available -> {
                alerts = state.alerts
                unavailableSections.remove(WeatherDetailSection.Alerts)
            }

            WeatherAlertFetchResult.Empty -> {
                alerts = emptyList()
                unavailableSections.remove(WeatherDetailSection.Alerts)
            }
        }
        emitStateIfReady()
    }

    private suspend fun refreshMinutePrecipitationSection(
        city: City,
        forceRefresh: Boolean,
    ) {
        val result = runCatching {
            getMinutePrecipitationUseCase(
                latitude = city.lat,
                longitude = city.lon,
                forceRefresh = forceRefresh,
            )
        }
        result.onFailure {
            minutePrecipitation = null
            isMinutePrecipitationUnsupported = false
            unavailableSections.add(WeatherDetailSection.MinutePrecipitation)
            emitStateIfReady()
            return
        }
        when (val state = result.getOrThrow()) {
            is MinutePrecipitationFetchResult.Available -> {
                minutePrecipitation = state.timeline
                isMinutePrecipitationUnsupported = false
                unavailableSections.remove(WeatherDetailSection.MinutePrecipitation)
            }

            MinutePrecipitationFetchResult.UnsupportedRegion -> {
                minutePrecipitation = null
                isMinutePrecipitationUnsupported = true
                unavailableSections.remove(WeatherDetailSection.MinutePrecipitation)
            }

            is MinutePrecipitationFetchResult.Failure -> {
                minutePrecipitation = null
                isMinutePrecipitationUnsupported = false
                unavailableSections.add(WeatherDetailSection.MinutePrecipitation)
            }
        }
        emitStateIfReady()
    }

    private suspend fun refreshAstronomySection(
        city: City,
        forceRefresh: Boolean,
    ) {
        val date = currentDateInCity(city.timeZone)
        val result = runCatching {
            getSunriseSunsetUseCase(
                locationId = city.id,
                date = date,
                forceRefresh = forceRefresh,
            )
        }
        result.onFailure {
            sunriseSunset = null
            unavailableSections.add(WeatherDetailSection.Astronomy)
            emitStateIfReady()
            return
        }
        when (val state = result.getOrThrow()) {
            is SunriseSunsetFetchResult.Available -> {
                sunriseSunset = state.sunriseSunset
                unavailableSections.remove(WeatherDetailSection.Astronomy)
            }

            is SunriseSunsetFetchResult.Failure -> {
                sunriseSunset = null
                unavailableSections.add(WeatherDetailSection.Astronomy)
            }
        }
        emitStateIfReady()
    }

    private suspend fun refreshIndicesSection(
        city: City,
        forceRefresh: Boolean,
    ) {
        val result = runCatching {
            getWeatherIndicesUseCase(
                locationId = city.id,
                forceRefresh = forceRefresh,
            )
        }
        result.onFailure {
            weatherIndices = null
            unavailableSections.add(WeatherDetailSection.Indices)
            emitStateIfReady()
            return
        }
        when (val state = result.getOrThrow()) {
            is WeatherIndicesFetchResult.Available -> {
                weatherIndices = state.weatherIndices
                unavailableSections.remove(WeatherDetailSection.Indices)
            }

            WeatherIndicesFetchResult.Empty -> {
                weatherIndices = null
                unavailableSections.remove(WeatherDetailSection.Indices)
            }

            is WeatherIndicesFetchResult.Failure -> {
                weatherIndices = null
                unavailableSections.add(WeatherDetailSection.Indices)
            }
        }
        emitStateIfReady()
    }

    private suspend fun refreshAirQualitySection(
        city: City,
        forceRefresh: Boolean,
    ) {
        val result = runCatching {
            getAirQualityUseCase(
                latitude = city.lat,
                longitude = city.lon,
                forceRefresh = forceRefresh,
            )
        }
        result.onFailure {
            airQuality = null
            isAirQualityUnsupported = false
            unavailableSections.add(WeatherDetailSection.AirQuality)
            emitStateIfReady()
            return
        }
        when (val state = result.getOrThrow()) {
            is AirQualityFetchResult.Available -> {
                airQuality = state.airQuality
                isAirQualityUnsupported = false
                unavailableSections.remove(WeatherDetailSection.AirQuality)
            }

            AirQualityFetchResult.UnsupportedRegion -> {
                airQuality = null
                isAirQualityUnsupported = true
                unavailableSections.remove(WeatherDetailSection.AirQuality)
            }

            is AirQualityFetchResult.Failure -> {
                airQuality = null
                isAirQualityUnsupported = false
                unavailableSections.add(WeatherDetailSection.AirQuality)
            }
        }
        emitStateIfReady()
    }

    private fun emitStateIfReady() {
        val city = activeCity ?: return
        val hourly = hourlyForecast
        val daily = dailyForecast
        if (hourly == null || daily == null) {
            _uiState.value = WeatherDetailUiState(state = WeatherDetailState.Loading)
            return
        }
        _uiState.value = if (unavailableSections.isEmpty()) {
            WeatherDetailUiState(
                state = WeatherDetailState.Content(
                    city = city,
                    hourlyForecast = hourly,
                    dailyForecast = daily,
                    minutePrecipitation = minutePrecipitation,
                    isMinutePrecipitationUnsupported = isMinutePrecipitationUnsupported,
                    sunriseSunset = sunriseSunset,
                    weatherIndices = weatherIndices,
                    alerts = alerts,
                    airQuality = airQuality,
                    isAirQualityUnsupported = isAirQualityUnsupported,
                ),
            )
        } else {
            WeatherDetailUiState(
                state = WeatherDetailState.PartialContent(
                    city = city,
                    hourlyForecast = hourly,
                    dailyForecast = daily,
                    minutePrecipitation = minutePrecipitation,
                    isMinutePrecipitationUnsupported = isMinutePrecipitationUnsupported,
                    sunriseSunset = sunriseSunset,
                    weatherIndices = weatherIndices,
                    alerts = alerts,
                    airQuality = airQuality,
                    isAirQualityUnsupported = isAirQualityUnsupported,
                    unavailableSections = unavailableSections.toSet(),
                ),
            )
        }
    }

    private fun currentDateInCity(timeZoneId: String): String {
        val zoneId = runCatching {
            ZoneId.of(timeZoneId)
        }.getOrDefault(ZoneId.systemDefault())
        return LocalDate.now(zoneId).format(DateTimeFormatter.BASIC_ISO_DATE)
    }
}
