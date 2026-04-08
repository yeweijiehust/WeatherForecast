package io.github.yeweijiehust.weatherforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.yeweijiehust.weatherforecast.data.local.entity.CurrentWeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrentWeatherDao {
    @Query(
        """
        SELECT * FROM current_weather_cache
        WHERE location_id = :locationId
          AND language = :language
          AND unit_system = :unitSystem
        LIMIT 1
        """,
    )
    fun observeCurrentWeather(
        locationId: String,
        language: String,
        unitSystem: String,
    ): Flow<CurrentWeatherEntity?>

    @Query(
        """
        SELECT * FROM current_weather_cache
        WHERE location_id = :locationId
          AND language = :language
          AND unit_system = :unitSystem
        LIMIT 1
        """,
    )
    suspend fun getCurrentWeather(
        locationId: String,
        language: String,
        unitSystem: String,
    ): CurrentWeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCurrentWeather(currentWeather: CurrentWeatherEntity)

    @Query("DELETE FROM current_weather_cache")
    suspend fun clearAll()
}
