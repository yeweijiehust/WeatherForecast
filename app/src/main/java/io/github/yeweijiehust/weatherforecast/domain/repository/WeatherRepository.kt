package io.github.yeweijiehust.weatherforecast.domain.repository

import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun observeCurrentWeather(cityId: String): Flow<CurrentWeather?>

    suspend fun refreshCurrentWeather(cityId: String)

    fun observeHourlyForecast(cityId: String): Flow<List<HourlyForecast>>

    suspend fun refreshHourlyForecast(cityId: String)
}
