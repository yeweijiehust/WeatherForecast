package io.github.yeweijiehust.weatherforecast.data.local.source

import io.github.yeweijiehust.weatherforecast.data.local.model.SavedCityLocalModel
import kotlinx.coroutines.flow.Flow

interface SavedCityLocalDataSource {
    fun observeSavedCities(): Flow<List<SavedCityLocalModel>>

    suspend fun getSavedCities(): List<SavedCityLocalModel>

    suspend fun getSavedCity(locationId: String): SavedCityLocalModel?

    suspend fun insertCity(city: SavedCityLocalModel): Boolean

    suspend fun deleteCity(locationId: String)

    suspend fun nextSortOrder(): Int
}
