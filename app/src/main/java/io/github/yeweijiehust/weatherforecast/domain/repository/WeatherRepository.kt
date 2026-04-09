package io.github.yeweijiehust.weatherforecast.domain.repository

import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import kotlinx.coroutines.flow.Flow

interface CurrentWeatherRepository {
    fun observeCurrentWeather(cityId: String): Flow<CurrentWeather?>

    suspend fun refreshCurrentWeather(
        cityId: String,
        forceRefresh: Boolean = false,
    )
}

interface HourlyForecastRepository {
    fun observeHourlyForecast(cityId: String): Flow<List<HourlyForecast>>

    suspend fun refreshHourlyForecast(
        cityId: String,
        forceRefresh: Boolean = false,
    )
}

interface DailyForecastRepository {
    fun observeDailyForecast(cityId: String): Flow<List<DailyForecast>>

    suspend fun refreshDailyForecast(
        cityId: String,
        forceRefresh: Boolean = false,
    )
}

interface WeatherAlertsRepository {
    suspend fun fetchWeatherAlerts(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean = false,
    ): WeatherAlertFetchResult
}

interface AirQualityRepository {
    suspend fun fetchAirQuality(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean = false,
    ): AirQualityFetchResult
}

interface MinutePrecipitationRepository {
    suspend fun fetchMinutePrecipitation(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean = false,
    ): MinutePrecipitationFetchResult
}

interface SunriseSunsetRepository {
    suspend fun fetchSunriseSunset(
        locationId: String,
        date: String,
        forceRefresh: Boolean = false,
    ): SunriseSunsetFetchResult
}

interface WeatherIndicesRepository {
    suspend fun fetchWeatherIndices(
        locationId: String,
        forceRefresh: Boolean = false,
    ): WeatherIndicesFetchResult
}

interface WeatherRepository :
    CurrentWeatherRepository,
    HourlyForecastRepository,
    DailyForecastRepository,
    WeatherAlertsRepository,
    AirQualityRepository,
    MinutePrecipitationRepository,
    SunriseSunsetRepository,
    WeatherIndicesRepository
