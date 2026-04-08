package io.github.yeweijiehust.weatherforecast.data.local.source

import kotlinx.coroutines.flow.Flow

interface DefaultCityPreferencesDataSource {
    fun observeDefaultCityId(): Flow<String?>

    suspend fun getDefaultCityId(): String?

    suspend fun setDefaultCityId(cityId: String?)
}
