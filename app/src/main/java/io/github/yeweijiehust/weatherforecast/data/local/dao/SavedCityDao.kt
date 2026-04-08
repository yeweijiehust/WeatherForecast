package io.github.yeweijiehust.weatherforecast.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.yeweijiehust.weatherforecast.data.local.entity.SavedCityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCityDao {
    @Query("SELECT * FROM saved_city ORDER BY sort_order ASC, created_at ASC")
    fun observeSavedCities(): Flow<List<SavedCityEntity>>

    @Query("SELECT * FROM saved_city ORDER BY sort_order ASC, created_at ASC")
    suspend fun getSavedCities(): List<SavedCityEntity>

    @Query("SELECT * FROM saved_city WHERE location_id = :locationId LIMIT 1")
    suspend fun getSavedCity(locationId: String): SavedCityEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCity(city: SavedCityEntity): Long

    @Query("DELETE FROM saved_city WHERE location_id = :locationId")
    suspend fun deleteCity(locationId: String)

    @Query("SELECT MAX(sort_order) FROM saved_city")
    suspend fun getMaxSortOrder(): Int?
}
