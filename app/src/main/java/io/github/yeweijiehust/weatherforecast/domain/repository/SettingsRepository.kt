package io.github.yeweijiehust.weatherforecast.domain.repository

import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeAppSettings(): Flow<AppSettings>

    suspend fun getCurrentSettings(): AppSettings

    suspend fun updateLanguage(language: AppLanguage)

    suspend fun updateUnitSystem(unitSystem: UnitSystem)

    suspend fun clearWeatherCache()
}
