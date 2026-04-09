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

interface WeatherRepository {
    fun observeCurrentWeather(cityId: String): Flow<CurrentWeather?>

    suspend fun refreshCurrentWeather(
        cityId: String,
        forceRefresh: Boolean = false,
    )

    fun observeHourlyForecast(cityId: String): Flow<List<HourlyForecast>>

    suspend fun refreshHourlyForecast(
        cityId: String,
        forceRefresh: Boolean = false,
    )

    fun observeDailyForecast(cityId: String): Flow<List<DailyForecast>>

    suspend fun refreshDailyForecast(
        cityId: String,
        forceRefresh: Boolean = false,
    )

    suspend fun fetchWeatherAlerts(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean = false,
    ): WeatherAlertFetchResult

    suspend fun fetchAirQuality(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean = false,
    ): AirQualityFetchResult

    suspend fun fetchMinutePrecipitation(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean = false,
    ): MinutePrecipitationFetchResult

    suspend fun fetchSunriseSunset(
        locationId: String,
        date: String,
        forceRefresh: Boolean = false,
    ): SunriseSunsetFetchResult

    suspend fun fetchWeatherIndices(
        locationId: String,
        forceRefresh: Boolean = false,
    ): WeatherIndicesFetchResult
}
