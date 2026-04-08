package io.github.yeweijiehust.weatherforecast.data.local.source

import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import kotlinx.coroutines.flow.Flow

interface AppSettingsPreferencesDataSource {
    fun observeAppSettings(): Flow<AppSettings>

    suspend fun getCurrentSettings(): AppSettings

    suspend fun updateLanguage(language: AppLanguage)

    suspend fun updateUnitSystem(unitSystem: UnitSystem)
}
