package io.github.yeweijiehust.weatherforecast.data.local.source

import io.github.yeweijiehust.weatherforecast.data.local.dao.HourlyForecastDao
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toEntity
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toLocalModel
import io.github.yeweijiehust.weatherforecast.data.local.model.HourlyForecastLocalModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomHourlyForecastLocalDataSource @Inject constructor(
    private val hourlyForecastDao: HourlyForecastDao,
) : HourlyForecastLocalDataSource {
    override fun observeHourlyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
    ): Flow<List<HourlyForecastLocalModel>> {
        return hourlyForecastDao.observeHourlyForecast(
            locationId = cityId,
            language = language,
            unitSystem = unitSystem,
        ).map { entities ->
            entities.map { entity -> entity.toLocalModel() }
        }
    }

    override suspend fun getHourlyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
    ): List<HourlyForecastLocalModel> {
        return hourlyForecastDao.getHourlyForecast(
            locationId = cityId,
            language = language,
            unitSystem = unitSystem,
        ).map { entity ->
            entity.toLocalModel()
        }
    }

    override suspend fun replaceHourlyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
        hourlyForecast: List<HourlyForecastLocalModel>,
    ) {
        hourlyForecastDao.replaceHourlyForecast(
            locationId = cityId,
            language = language,
            unitSystem = unitSystem,
            hourlyForecast = hourlyForecast.map { localModel ->
                localModel.toEntity()
            },
        )
    }

    override suspend fun clearHourlyForecastCache() {
        hourlyForecastDao.clearAll()
    }
}
