package io.github.yeweijiehust.weatherforecast.domain.repository

import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun observeCurrentWeather(cityId: String): Flow<CurrentWeather?>

    suspend fun refreshCurrentWeather(cityId: String)
}
