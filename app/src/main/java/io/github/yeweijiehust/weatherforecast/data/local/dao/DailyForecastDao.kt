package io.github.yeweijiehust.weatherforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.yeweijiehust.weatherforecast.data.local.entity.DailyForecastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyForecastDao {
    @Query(
        """
        SELECT * FROM daily_forecast_cache
        WHERE location_id = :locationId
          AND language = :language
          AND unit_system = :unitSystem
        ORDER BY forecast_date ASC
        """,
    )
    fun observeDailyForecast(
        locationId: String,
        language: String,
        unitSystem: String,
    ): Flow<List<DailyForecastEntity>>

    @Query(
        """
        SELECT * FROM daily_forecast_cache
        WHERE location_id = :locationId
          AND language = :language
          AND unit_system = :unitSystem
        ORDER BY forecast_date ASC
        """,
    )
    suspend fun getDailyForecast(
        locationId: String,
        language: String,
        unitSystem: String,
    ): List<DailyForecastEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyForecast(dailyForecast: List<DailyForecastEntity>)

    @Query(
        """
        DELETE FROM daily_forecast_cache
        WHERE location_id = :locationId
          AND language = :language
          AND unit_system = :unitSystem
        """,
    )
    suspend fun clearForCityWithSettings(
        locationId: String,
        language: String,
        unitSystem: String,
    )

    @Transaction
    suspend fun replaceDailyForecast(
        locationId: String,
        language: String,
        unitSystem: String,
        dailyForecast: List<DailyForecastEntity>,
    ) {
        clearForCityWithSettings(
            locationId = locationId,
            language = language,
            unitSystem = unitSystem,
        )
        if (dailyForecast.isNotEmpty()) {
            upsertDailyForecast(dailyForecast)
        }
    }

    @Query("DELETE FROM daily_forecast_cache")
    suspend fun clearAll()
}
