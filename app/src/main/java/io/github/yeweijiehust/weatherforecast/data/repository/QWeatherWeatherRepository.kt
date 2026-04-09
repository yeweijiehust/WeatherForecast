package io.github.yeweijiehust.weatherforecast.data.repository

import io.github.yeweijiehust.weatherforecast.data.local.source.CurrentWeatherLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DailyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.HourlyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.remote.api.WeatherApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class QWeatherWeatherRepository @Inject constructor(
    weatherApiService: WeatherApiService,
    qWeatherConfig: QWeatherConfig,
    currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
    hourlyForecastLocalDataSource: HourlyForecastLocalDataSource,
    dailyForecastLocalDataSource: DailyForecastLocalDataSource,
    settingsRepository: SettingsRepository,
) : WeatherRepository {
    private val policyStore = WeatherRequestPolicyStore()

    private val forecastRepository = QWeatherForecastRepository(
        weatherApiService = weatherApiService,
        qWeatherConfig = qWeatherConfig,
        currentWeatherLocalDataSource = currentWeatherLocalDataSource,
        hourlyForecastLocalDataSource = hourlyForecastLocalDataSource,
        dailyForecastLocalDataSource = dailyForecastLocalDataSource,
        settingsRepository = settingsRepository,
        policyStore = policyStore,
    )

    private val secondaryRepository = QWeatherSecondaryRepository(
        weatherApiService = weatherApiService,
        qWeatherConfig = qWeatherConfig,
        settingsRepository = settingsRepository,
        policyStore = policyStore,
    )

    override fun observeCurrentWeather(cityId: String): Flow<CurrentWeather?> {
        return forecastRepository.observeCurrentWeather(cityId)
    }

    override suspend fun refreshCurrentWeather(
        cityId: String,
        forceRefresh: Boolean,
    ) {
        forecastRepository.refreshCurrentWeather(
            cityId = cityId,
            forceRefresh = forceRefresh,
        )
    }

    override fun observeHourlyForecast(cityId: String): Flow<List<HourlyForecast>> {
        return forecastRepository.observeHourlyForecast(cityId)
    }

    override suspend fun refreshHourlyForecast(
        cityId: String,
        forceRefresh: Boolean,
    ) {
        forecastRepository.refreshHourlyForecast(
            cityId = cityId,
            forceRefresh = forceRefresh,
        )
    }

    override fun observeDailyForecast(cityId: String): Flow<List<DailyForecast>> {
        return forecastRepository.observeDailyForecast(cityId)
    }

    override suspend fun refreshDailyForecast(
        cityId: String,
        forceRefresh: Boolean,
    ) {
        forecastRepository.refreshDailyForecast(
            cityId = cityId,
            forceRefresh = forceRefresh,
        )
    }

    override suspend fun fetchWeatherAlerts(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean,
    ): WeatherAlertFetchResult {
        return secondaryRepository.fetchWeatherAlerts(
            latitude = latitude,
            longitude = longitude,
            forceRefresh = forceRefresh,
        )
    }

    override suspend fun fetchAirQuality(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean,
    ): AirQualityFetchResult {
        return secondaryRepository.fetchAirQuality(
            latitude = latitude,
            longitude = longitude,
            forceRefresh = forceRefresh,
        )
    }

    override suspend fun fetchMinutePrecipitation(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean,
    ): MinutePrecipitationFetchResult {
        return secondaryRepository.fetchMinutePrecipitation(
            latitude = latitude,
            longitude = longitude,
            forceRefresh = forceRefresh,
        )
    }

    override suspend fun fetchSunriseSunset(
        locationId: String,
        date: String,
        forceRefresh: Boolean,
    ): SunriseSunsetFetchResult {
        return secondaryRepository.fetchSunriseSunset(
            locationId = locationId,
            date = date,
            forceRefresh = forceRefresh,
        )
    }

    override suspend fun fetchWeatherIndices(
        locationId: String,
        forceRefresh: Boolean,
    ): WeatherIndicesFetchResult {
        return secondaryRepository.fetchWeatherIndices(
            locationId = locationId,
            forceRefresh = forceRefresh,
        )
    }
}
