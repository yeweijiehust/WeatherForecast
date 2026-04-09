package io.github.yeweijiehust.weatherforecast.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.ui.UiText
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val _events = MutableSharedFlow<WeatherDetailEvent>()
    val events: SharedFlow<WeatherDetailEvent> = _events.asSharedFlow()

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
    private val refreshGuardLock = Any()
    private val inFlightRefreshKeys = mutableSetOf<RefreshKey>()

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
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.HourlyForecast,
            forceRefresh = true,
            notifyFailure = true,
        )
    }

    fun retryDailySection() {
        val city = activeCity ?: return
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.DailyForecast,
            forceRefresh = true,
            notifyFailure = true,
        )
    }

    fun retryAlertsSection() {
        val city = activeCity ?: return
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.Alerts,
            forceRefresh = true,
            notifyFailure = true,
        )
    }

    fun retryMinutePrecipitationSection() {
        val city = activeCity ?: return
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.MinutePrecipitation,
            forceRefresh = true,
            notifyFailure = true,
        )
    }

    fun retryAstronomySection() {
        val city = activeCity ?: return
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.Astronomy,
            forceRefresh = true,
            notifyFailure = true,
        )
    }

    fun retryIndicesSection() {
        val city = activeCity ?: return
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.Indices,
            forceRefresh = true,
            notifyFailure = true,
        )
    }

    fun retryAirQualitySection() {
        val city = activeCity ?: return
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.AirQuality,
            forceRefresh = true,
            notifyFailure = true,
        )
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
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.HourlyForecast,
            forceRefresh = false,
            notifyFailure = false,
        )
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.DailyForecast,
            forceRefresh = false,
            notifyFailure = false,
        )
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.MinutePrecipitation,
            forceRefresh = false,
            notifyFailure = false,
        )
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.Astronomy,
            forceRefresh = false,
            notifyFailure = false,
        )
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.Indices,
            forceRefresh = false,
            notifyFailure = false,
        )
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.Alerts,
            forceRefresh = false,
            notifyFailure = false,
        )
        launchSectionRefresh(
            cityId = city.id,
            section = WeatherDetailSection.AirQuality,
            forceRefresh = false,
            notifyFailure = false,
        )
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

    private fun launchSectionRefresh(
        cityId: String,
        section: WeatherDetailSection,
        forceRefresh: Boolean,
        notifyFailure: Boolean,
    ) {
        viewModelScope.launch {
            val refreshKey = RefreshKey(
                cityId = cityId,
                section = section,
            )
            if (!tryAcquireRefresh(refreshKey)) {
                return@launch
            }
            try {
                when (section) {
                    WeatherDetailSection.HourlyForecast -> refreshHourlySection(
                        cityId = cityId,
                        forceRefresh = forceRefresh,
                        notifyFailure = notifyFailure,
                    )

                    WeatherDetailSection.DailyForecast -> refreshDailySection(
                        cityId = cityId,
                        forceRefresh = forceRefresh,
                        notifyFailure = notifyFailure,
                    )

                    WeatherDetailSection.Alerts -> {
                        val city = activeCity ?: return@launch
                        if (city.id != cityId) return@launch
                        refreshAlertsSection(
                            city = city,
                            forceRefresh = forceRefresh,
                            notifyFailure = notifyFailure,
                        )
                    }

                    WeatherDetailSection.AirQuality -> {
                        val city = activeCity ?: return@launch
                        if (city.id != cityId) return@launch
                        refreshAirQualitySection(
                            city = city,
                            forceRefresh = forceRefresh,
                            notifyFailure = notifyFailure,
                        )
                    }

                    WeatherDetailSection.MinutePrecipitation -> {
                        val city = activeCity ?: return@launch
                        if (city.id != cityId) return@launch
                        refreshMinutePrecipitationSection(
                            city = city,
                            forceRefresh = forceRefresh,
                            notifyFailure = notifyFailure,
                        )
                    }

                    WeatherDetailSection.Astronomy -> {
                        val city = activeCity ?: return@launch
                        if (city.id != cityId) return@launch
                        refreshAstronomySection(
                            city = city,
                            forceRefresh = forceRefresh,
                            notifyFailure = notifyFailure,
                        )
                    }

                    WeatherDetailSection.Indices -> {
                        val city = activeCity ?: return@launch
                        if (city.id != cityId) return@launch
                        refreshIndicesSection(
                            city = city,
                            forceRefresh = forceRefresh,
                            notifyFailure = notifyFailure,
                        )
                    }
                }
            } finally {
                releaseRefresh(refreshKey)
            }
        }
    }

    private suspend fun refreshHourlySection(
        cityId: String,
        forceRefresh: Boolean,
        notifyFailure: Boolean,
    ) {
        if (activeCity?.id != cityId) return
        val result = runCatching {
            refreshHourlyForecastUseCase(
                cityId = cityId,
                forceRefresh = forceRefresh,
            )
        }
        if (result.isFailure) {
            if (hourlyForecast.orEmpty().isEmpty()) {
                unavailableSections.add(WeatherDetailSection.HourlyForecast)
            }
            if (notifyFailure) {
                emitRetryFailedMessage()
            }
        } else if (result.isSuccess) {
            unavailableSections.remove(WeatherDetailSection.HourlyForecast)
        }
        emitStateIfReady()
    }

    private suspend fun refreshDailySection(
        cityId: String,
        forceRefresh: Boolean,
        notifyFailure: Boolean,
    ) {
        if (activeCity?.id != cityId) return
        val result = runCatching {
            refreshDailyForecastUseCase(
                cityId = cityId,
                forceRefresh = forceRefresh,
            )
        }
        if (result.isFailure) {
            if (dailyForecast.orEmpty().isEmpty()) {
                unavailableSections.add(WeatherDetailSection.DailyForecast)
            }
            if (notifyFailure) {
                emitRetryFailedMessage()
            }
        } else if (result.isSuccess) {
            unavailableSections.remove(WeatherDetailSection.DailyForecast)
        }
        emitStateIfReady()
    }

    private suspend fun refreshAlertsSection(
        city: City,
        forceRefresh: Boolean,
        notifyFailure: Boolean,
    ) {
        if (activeCity?.id != city.id) return
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
            if (notifyFailure) {
                emitRetryFailedMessage()
            }
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
        notifyFailure: Boolean,
    ) {
        if (activeCity?.id != city.id) return
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
            if (notifyFailure) {
                emitRetryFailedMessage()
            }
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
                if (notifyFailure) {
                    emitRetryFailedMessage()
                }
            }
        }
        emitStateIfReady()
    }

    private suspend fun refreshAstronomySection(
        city: City,
        forceRefresh: Boolean,
        notifyFailure: Boolean,
    ) {
        if (activeCity?.id != city.id) return
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
            if (notifyFailure) {
                emitRetryFailedMessage()
            }
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
                if (notifyFailure) {
                    emitRetryFailedMessage()
                }
            }
        }
        emitStateIfReady()
    }

    private suspend fun refreshIndicesSection(
        city: City,
        forceRefresh: Boolean,
        notifyFailure: Boolean,
    ) {
        if (activeCity?.id != city.id) return
        val result = runCatching {
            getWeatherIndicesUseCase(
                locationId = city.id,
                forceRefresh = forceRefresh,
            )
        }
        result.onFailure {
            weatherIndices = null
            unavailableSections.add(WeatherDetailSection.Indices)
            if (notifyFailure) {
                emitRetryFailedMessage()
            }
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
                if (notifyFailure) {
                    emitRetryFailedMessage()
                }
            }
        }
        emitStateIfReady()
    }

    private suspend fun refreshAirQualitySection(
        city: City,
        forceRefresh: Boolean,
        notifyFailure: Boolean,
    ) {
        if (activeCity?.id != city.id) return
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
            if (notifyFailure) {
                emitRetryFailedMessage()
            }
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
                if (notifyFailure) {
                    emitRetryFailedMessage()
                }
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

    private fun tryAcquireRefresh(
        refreshKey: RefreshKey,
    ): Boolean {
        return synchronized(refreshGuardLock) {
            inFlightRefreshKeys.add(refreshKey)
        }
    }

    private fun releaseRefresh(
        refreshKey: RefreshKey,
    ) {
        synchronized(refreshGuardLock) {
            inFlightRefreshKeys.remove(refreshKey)
        }
    }

    private suspend fun emitRetryFailedMessage() {
        _events.emit(
            WeatherDetailEvent.ShowMessage(
                message = UiText.StringResource(R.string.snackbar_refresh_failed),
            ),
        )
    }

    private fun currentDateInCity(timeZoneId: String): String {
        val zoneId = runCatching {
            ZoneId.of(timeZoneId)
        }.getOrDefault(ZoneId.systemDefault())
        return LocalDate.now(zoneId).format(DateTimeFormatter.BASIC_ISO_DATE)
    }

    private data class RefreshKey(
        val cityId: String,
        val section: WeatherDetailSection,
    )
}
