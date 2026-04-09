package io.github.yeweijiehust.weatherforecast.data.local.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreAppSettingsPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : AppSettingsPreferencesDataSource {
    override fun observeAppSettings(): Flow<AppSettings> {
        return dataStore.data.map(::preferencesToSettings)
    }

    override suspend fun getCurrentSettings(): AppSettings {
        return preferencesToSettings(dataStore.data.first())
    }

    override suspend fun updateLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[APP_LANGUAGE_KEY] = language.storageValue
        }
    }

    override suspend fun updateUnitSystem(unitSystem: UnitSystem) {
        dataStore.edit { preferences ->
            preferences[UNIT_SYSTEM_KEY] = unitSystem.storageValue
        }
    }

    private fun preferencesToSettings(preferences: Preferences): AppSettings {
        return AppSettings(
            language = AppLanguage.entries.firstOrNull {
                it.storageValue == preferences[APP_LANGUAGE_KEY]
            } ?: AppLanguage.SimplifiedChinese,
            unitSystem = UnitSystem.entries.firstOrNull {
                it.storageValue == preferences[UNIT_SYSTEM_KEY]
            } ?: UnitSystem.Metric,
        )
    }

    private companion object {
        val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
        val UNIT_SYSTEM_KEY = stringPreferencesKey("unit_system")
    }
}
