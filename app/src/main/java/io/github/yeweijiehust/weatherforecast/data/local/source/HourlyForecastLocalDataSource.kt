package io.github.yeweijiehust.weatherforecast.data.local.source

import io.github.yeweijiehust.weatherforecast.data.local.model.HourlyForecastLocalModel
import kotlinx.coroutines.flow.Flow

interface HourlyForecastLocalDataSource {
    fun observeHourlyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
    ): Flow<List<HourlyForecastLocalModel>>

    suspend fun getHourlyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
    ): List<HourlyForecastLocalModel>

    suspend fun replaceHourlyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
        hourlyForecast: List<HourlyForecastLocalModel>,
    )

    suspend fun clearHourlyForecastCache()
}
