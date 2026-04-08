package io.github.yeweijiehust.weatherforecast.data.local.source

import io.github.yeweijiehust.weatherforecast.data.local.dao.CurrentWeatherDao
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toEntity
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toLocalModel
import io.github.yeweijiehust.weatherforecast.data.local.model.CurrentWeatherLocalModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCurrentWeatherLocalDataSource @Inject constructor(
    private val currentWeatherDao: CurrentWeatherDao,
) : CurrentWeatherLocalDataSource {
    override fun observeCurrentWeather(
        cityId: String,
        language: String,
        unitSystem: String,
    ): Flow<CurrentWeatherLocalModel?> {
        return currentWeatherDao.observeCurrentWeather(
            locationId = cityId,
            language = language,
            unitSystem = unitSystem,
        ).map { entity ->
            entity?.toLocalModel()
        }
    }

    override suspend fun getCurrentWeather(
        cityId: String,
        language: String,
        unitSystem: String,
    ): CurrentWeatherLocalModel? {
        return currentWeatherDao.getCurrentWeather(
            locationId = cityId,
            language = language,
            unitSystem = unitSystem,
        )?.toLocalModel()
    }

    override suspend fun upsertCurrentWeather(currentWeather: CurrentWeatherLocalModel) {
        currentWeatherDao.upsertCurrentWeather(currentWeather.toEntity())
    }

    override suspend fun clearCurrentWeatherCache() {
        currentWeatherDao.clearAll()
    }
}
