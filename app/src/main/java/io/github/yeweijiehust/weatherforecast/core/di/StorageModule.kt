package io.github.yeweijiehust.weatherforecast.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.yeweijiehust.weatherforecast.data.local.dao.CurrentWeatherDao
import io.github.yeweijiehust.weatherforecast.data.local.dao.DailyForecastDao
import io.github.yeweijiehust.weatherforecast.data.local.dao.HourlyForecastDao
import io.github.yeweijiehust.weatherforecast.data.local.dao.SavedCityDao
import io.github.yeweijiehust.weatherforecast.data.local.db.WeatherForecastDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): WeatherForecastDatabase {
        return Room.databaseBuilder(
            context,
            WeatherForecastDatabase::class.java,
            "weather_forecast.db",
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideSavedCityDao(
        database: WeatherForecastDatabase,
    ): SavedCityDao = database.savedCityDao()

    @Provides
    fun provideCurrentWeatherDao(
        database: WeatherForecastDatabase,
    ): CurrentWeatherDao = database.currentWeatherDao()

    @Provides
    fun provideHourlyForecastDao(
        database: WeatherForecastDatabase,
    ): HourlyForecastDao = database.hourlyForecastDao()

    @Provides
    fun provideDailyForecastDao(
        database: WeatherForecastDatabase,
    ): DailyForecastDao = database.dailyForecastDao()
}
