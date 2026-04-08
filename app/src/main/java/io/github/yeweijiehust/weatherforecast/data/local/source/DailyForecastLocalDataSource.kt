package io.github.yeweijiehust.weatherforecast.data.local.source

import io.github.yeweijiehust.weatherforecast.data.local.model.DailyForecastLocalModel
import kotlinx.coroutines.flow.Flow

interface DailyForecastLocalDataSource {
    fun observeDailyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
    ): Flow<List<DailyForecastLocalModel>>

    suspend fun getDailyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
    ): List<DailyForecastLocalModel>

    suspend fun replaceDailyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
        dailyForecast: List<DailyForecastLocalModel>,
    )

    suspend fun clearDailyForecastCache()
}
