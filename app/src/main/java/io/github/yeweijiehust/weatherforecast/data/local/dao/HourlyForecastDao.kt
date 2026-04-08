package io.github.yeweijiehust.weatherforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.yeweijiehust.weatherforecast.data.local.entity.HourlyForecastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HourlyForecastDao {
    @Query(
        """
        SELECT * FROM hourly_forecast_cache
        WHERE location_id = :locationId
          AND language = :language
          AND unit_system = :unitSystem
        ORDER BY forecast_time ASC
        """,
    )
    fun observeHourlyForecast(
        locationId: String,
        language: String,
        unitSystem: String,
    ): Flow<List<HourlyForecastEntity>>

    @Query(
        """
        SELECT * FROM hourly_forecast_cache
        WHERE location_id = :locationId
          AND language = :language
          AND unit_system = :unitSystem
        ORDER BY forecast_time ASC
        """,
    )
    suspend fun getHourlyForecast(
        locationId: String,
        language: String,
        unitSystem: String,
    ): List<HourlyForecastEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHourlyForecast(hourlyForecast: List<HourlyForecastEntity>)

    @Query(
        """
        DELETE FROM hourly_forecast_cache
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
    suspend fun replaceHourlyForecast(
        locationId: String,
        language: String,
        unitSystem: String,
        hourlyForecast: List<HourlyForecastEntity>,
    ) {
        clearForCityWithSettings(
            locationId = locationId,
            language = language,
            unitSystem = unitSystem,
        )
        if (hourlyForecast.isNotEmpty()) {
            upsertHourlyForecast(hourlyForecast)
        }
    }

    @Query("DELETE FROM hourly_forecast_cache")
    suspend fun clearAll()
}
