package io.github.yeweijiehust.weatherforecast.data.repository

import io.github.yeweijiehust.weatherforecast.data.local.source.AppSettingsPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.WeatherCacheCleaner
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class DefaultSettingsRepository @Inject constructor(
    private val appSettingsPreferencesDataSource: AppSettingsPreferencesDataSource,
    private val weatherCacheCleaner: WeatherCacheCleaner,
) : SettingsRepository {
    override fun observeAppSettings(): Flow<AppSettings> {
        return appSettingsPreferencesDataSource.observeAppSettings()
    }

    override suspend fun getCurrentSettings(): AppSettings {
        return appSettingsPreferencesDataSource.getCurrentSettings()
    }

    override suspend fun updateLanguage(language: AppLanguage) {
        appSettingsPreferencesDataSource.updateLanguage(language)
    }

    override suspend fun updateUnitSystem(unitSystem: UnitSystem) {
        appSettingsPreferencesDataSource.updateUnitSystem(unitSystem)
    }

    override suspend fun clearWeatherCache() {
        weatherCacheCleaner.clearWeatherCache()
    }
}
