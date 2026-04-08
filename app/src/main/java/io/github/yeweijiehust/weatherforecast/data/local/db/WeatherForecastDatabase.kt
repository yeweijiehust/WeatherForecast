package io.github.yeweijiehust.weatherforecast.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.yeweijiehust.weatherforecast.data.local.dao.SavedCityDao
import io.github.yeweijiehust.weatherforecast.data.local.entity.SavedCityEntity

@Database(
    entities = [SavedCityEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WeatherForecastDatabase : RoomDatabase() {
    abstract fun savedCityDao(): SavedCityDao
}
