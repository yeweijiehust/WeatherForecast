package io.github.yeweijiehust.weatherforecast.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.yeweijiehust.weatherforecast.data.local.source.AppSettingsPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DefaultCityPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.SavedCityLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.WeatherCacheCleaner
import io.github.yeweijiehust.weatherforecast.data.remote.api.GeoApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.repository.DefaultSettingsRepository
import io.github.yeweijiehust.weatherforecast.data.repository.QWeatherCityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideCityRepository(
        geoApiService: GeoApiService,
        qWeatherConfig: QWeatherConfig,
        savedCityLocalDataSource: SavedCityLocalDataSource,
        defaultCityPreferencesDataSource: DefaultCityPreferencesDataSource,
    ): CityRepository = QWeatherCityRepository(
        geoApiService = geoApiService,
        qWeatherConfig = qWeatherConfig,
        savedCityLocalDataSource = savedCityLocalDataSource,
        defaultCityPreferencesDataSource = defaultCityPreferencesDataSource,
    )

    @Provides
    @Singleton
    fun provideSettingsRepository(
        appSettingsPreferencesDataSource: AppSettingsPreferencesDataSource,
        weatherCacheCleaner: WeatherCacheCleaner,
    ): SettingsRepository = DefaultSettingsRepository(
        appSettingsPreferencesDataSource = appSettingsPreferencesDataSource,
        weatherCacheCleaner = weatherCacheCleaner,
    )
}
