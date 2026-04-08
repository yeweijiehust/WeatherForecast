package io.github.yeweijiehust.weatherforecast.data.local.source

import io.github.yeweijiehust.weatherforecast.data.local.dao.SavedCityDao
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toEntity
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toLocalModel
import io.github.yeweijiehust.weatherforecast.data.local.model.SavedCityLocalModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSavedCityLocalDataSource @Inject constructor(
    private val savedCityDao: SavedCityDao,
) : SavedCityLocalDataSource {
    override fun observeSavedCities(): Flow<List<SavedCityLocalModel>> {
        return savedCityDao.observeSavedCities().map { entities ->
            entities.map { it.toLocalModel() }
        }
    }

    override suspend fun getSavedCities(): List<SavedCityLocalModel> {
        return savedCityDao.getSavedCities().map { it.toLocalModel() }
    }

    override suspend fun getSavedCity(locationId: String): SavedCityLocalModel? {
        return savedCityDao.getSavedCity(locationId)?.toLocalModel()
    }

    override suspend fun insertCity(city: SavedCityLocalModel): Boolean {
        return savedCityDao.insertCity(city.toEntity()) != -1L
    }

    override suspend fun deleteCity(locationId: String) {
        savedCityDao.deleteCity(locationId)
    }

    override suspend fun nextSortOrder(): Int {
        return (savedCityDao.getMaxSortOrder() ?: -1) + 1
    }
}
