package io.github.yeweijiehust.weatherforecast.data.local.source

import io.github.yeweijiehust.weatherforecast.data.local.model.CurrentWeatherLocalModel
import kotlinx.coroutines.flow.Flow

interface CurrentWeatherLocalDataSource {
    fun observeCurrentWeather(
        cityId: String,
        language: String,
        unitSystem: String,
    ): Flow<CurrentWeatherLocalModel?>

    suspend fun getCurrentWeather(
        cityId: String,
        language: String,
        unitSystem: String,
    ): CurrentWeatherLocalModel?

    suspend fun upsertCurrentWeather(currentWeather: CurrentWeatherLocalModel)

    suspend fun clearCurrentWeatherCache()
}
