package io.github.yeweijiehust.weatherforecast.domain.repository

import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun observeCurrentWeather(cityId: String): Flow<CurrentWeather?>

    suspend fun refreshCurrentWeather(cityId: String)

    fun observeHourlyForecast(cityId: String): Flow<List<HourlyForecast>>

    suspend fun refreshHourlyForecast(cityId: String)

    fun observeDailyForecast(cityId: String): Flow<List<DailyForecast>>

    suspend fun refreshDailyForecast(cityId: String)

    suspend fun fetchWeatherAlerts(
        latitude: String,
        longitude: String,
    ): WeatherAlertFetchResult

    suspend fun fetchAirQuality(
        latitude: String,
        longitude: String,
    ): AirQualityFetchResult

    suspend fun fetchMinutePrecipitation(
        latitude: String,
        longitude: String,
    ): MinutePrecipitationFetchResult

    suspend fun fetchSunriseSunset(
        locationId: String,
        date: String,
    ): SunriseSunsetFetchResult
}
