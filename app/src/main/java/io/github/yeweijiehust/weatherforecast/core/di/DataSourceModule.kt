package io.github.yeweijiehust.weatherforecast.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.yeweijiehust.weatherforecast.data.local.source.AppSettingsPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.CurrentWeatherLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DataStoreAppSettingsPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DataStoreDefaultCityPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DefaultCityPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.RoomCurrentWeatherLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.RoomWeatherCacheCleaner
import io.github.yeweijiehust.weatherforecast.data.local.source.RoomSavedCityLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.SavedCityLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.WeatherCacheCleaner

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    abstract fun bindSavedCityLocalDataSource(
        roomSavedCityLocalDataSource: RoomSavedCityLocalDataSource,
    ): SavedCityLocalDataSource

    @Binds
    abstract fun bindDefaultCityPreferencesDataSource(
        dataStoreDefaultCityPreferencesDataSource: DataStoreDefaultCityPreferencesDataSource,
    ): DefaultCityPreferencesDataSource

    @Binds
    abstract fun bindAppSettingsPreferencesDataSource(
        dataStoreAppSettingsPreferencesDataSource: DataStoreAppSettingsPreferencesDataSource,
    ): AppSettingsPreferencesDataSource

    @Binds
    abstract fun bindCurrentWeatherLocalDataSource(
        roomCurrentWeatherLocalDataSource: RoomCurrentWeatherLocalDataSource,
    ): CurrentWeatherLocalDataSource

    @Binds
    abstract fun bindWeatherCacheCleaner(
        roomWeatherCacheCleaner: RoomWeatherCacheCleaner,
    ): WeatherCacheCleaner
}
