package io.github.yeweijiehust.weatherforecast.data.local.source

import io.github.yeweijiehust.weatherforecast.data.local.dao.DailyForecastDao
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toEntity
import io.github.yeweijiehust.weatherforecast.data.local.mapper.toLocalModel
import io.github.yeweijiehust.weatherforecast.data.local.model.DailyForecastLocalModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDailyForecastLocalDataSource @Inject constructor(
    private val dailyForecastDao: DailyForecastDao,
) : DailyForecastLocalDataSource {
    override fun observeDailyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
    ): Flow<List<DailyForecastLocalModel>> {
        return dailyForecastDao.observeDailyForecast(
            locationId = cityId,
            language = language,
            unitSystem = unitSystem,
        ).map { entities ->
            entities.map { entity -> entity.toLocalModel() }
        }
    }

    override suspend fun getDailyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
    ): List<DailyForecastLocalModel> {
        return dailyForecastDao.getDailyForecast(
            locationId = cityId,
            language = language,
            unitSystem = unitSystem,
        ).map { entity ->
            entity.toLocalModel()
        }
    }

    override suspend fun replaceDailyForecast(
        cityId: String,
        language: String,
        unitSystem: String,
        dailyForecast: List<DailyForecastLocalModel>,
    ) {
        dailyForecastDao.replaceDailyForecast(
            locationId = cityId,
            language = language,
            unitSystem = unitSystem,
            dailyForecast = dailyForecast.map { localModel ->
                localModel.toEntity()
            },
        )
    }

    override suspend fun clearDailyForecastCache() {
        dailyForecastDao.clearAll()
    }
}
