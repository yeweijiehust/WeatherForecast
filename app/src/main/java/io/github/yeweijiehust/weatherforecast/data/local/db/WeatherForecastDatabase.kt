package io.github.yeweijiehust.weatherforecast.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.yeweijiehust.weatherforecast.data.local.dao.CurrentWeatherDao
import io.github.yeweijiehust.weatherforecast.data.local.dao.DailyForecastDao
import io.github.yeweijiehust.weatherforecast.data.local.dao.HourlyForecastDao
import io.github.yeweijiehust.weatherforecast.data.local.dao.SavedCityDao
import io.github.yeweijiehust.weatherforecast.data.local.entity.CurrentWeatherEntity
import io.github.yeweijiehust.weatherforecast.data.local.entity.DailyForecastEntity
import io.github.yeweijiehust.weatherforecast.data.local.entity.HourlyForecastEntity
import io.github.yeweijiehust.weatherforecast.data.local.entity.SavedCityEntity

@Database(
    entities = [
        SavedCityEntity::class,
        CurrentWeatherEntity::class,
        HourlyForecastEntity::class,
        DailyForecastEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class WeatherForecastDatabase : RoomDatabase() {
    abstract fun savedCityDao(): SavedCityDao

    abstract fun currentWeatherDao(): CurrentWeatherDao

    abstract fun hourlyForecastDao(): HourlyForecastDao

    abstract fun dailyForecastDao(): DailyForecastDao
}
