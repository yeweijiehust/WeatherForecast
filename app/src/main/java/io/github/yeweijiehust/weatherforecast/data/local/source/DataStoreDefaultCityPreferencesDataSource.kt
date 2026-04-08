package io.github.yeweijiehust.weatherforecast.data.local.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreDefaultCityPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : DefaultCityPreferencesDataSource {
    override fun observeDefaultCityId(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[DEFAULT_CITY_ID_KEY]
        }
    }

    override suspend fun getDefaultCityId(): String? {
        return dataStore.data.first()[DEFAULT_CITY_ID_KEY]
    }

    override suspend fun setDefaultCityId(cityId: String?) {
        dataStore.edit { preferences ->
            if (cityId == null) {
                preferences.remove(DEFAULT_CITY_ID_KEY)
            } else {
                preferences[DEFAULT_CITY_ID_KEY] = cityId
            }
        }
    }

    private companion object {
        val DEFAULT_CITY_ID_KEY = stringPreferencesKey("default_city_id")
    }
}
