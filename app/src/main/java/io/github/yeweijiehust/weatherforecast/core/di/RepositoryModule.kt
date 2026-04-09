package io.github.yeweijiehust.weatherforecast.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.yeweijiehust.weatherforecast.data.local.source.AppSettingsPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.CurrentWeatherLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DailyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DefaultCityPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.HourlyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.SavedCityLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.WeatherCacheCleaner
import io.github.yeweijiehust.weatherforecast.data.remote.api.GeoApiService
import io.github.yeweijiehust.weatherforecast.data.remote.api.WeatherApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.repository.DefaultSettingsRepository
import io.github.yeweijiehust.weatherforecast.data.repository.QWeatherCityRepository
import io.github.yeweijiehust.weatherforecast.data.repository.QWeatherWeatherRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.AirQualityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.CurrentWeatherRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.DailyForecastRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.HourlyForecastRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.MinutePrecipitationRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SunriseSunsetRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherAlertsRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherIndicesRepository
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

    @Provides
    @Singleton
    fun provideQWeatherWeatherRepository(
        weatherApiService: WeatherApiService,
        qWeatherConfig: QWeatherConfig,
        currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
        hourlyForecastLocalDataSource: HourlyForecastLocalDataSource,
        dailyForecastLocalDataSource: DailyForecastLocalDataSource,
        settingsRepository: SettingsRepository,
    ): QWeatherWeatherRepository = QWeatherWeatherRepository(
        weatherApiService = weatherApiService,
        qWeatherConfig = qWeatherConfig,
        currentWeatherLocalDataSource = currentWeatherLocalDataSource,
        hourlyForecastLocalDataSource = hourlyForecastLocalDataSource,
        dailyForecastLocalDataSource = dailyForecastLocalDataSource,
        settingsRepository = settingsRepository,
    )

    @Provides
    @Singleton
    fun provideWeatherRepository(
        repository: QWeatherWeatherRepository,
    ): WeatherRepository = repository

    @Provides
    @Singleton
    fun provideCurrentWeatherRepository(
        repository: QWeatherWeatherRepository,
    ): CurrentWeatherRepository = repository

    @Provides
    @Singleton
    fun provideHourlyForecastRepository(
        repository: QWeatherWeatherRepository,
    ): HourlyForecastRepository = repository

    @Provides
    @Singleton
    fun provideDailyForecastRepository(
        repository: QWeatherWeatherRepository,
    ): DailyForecastRepository = repository

    @Provides
    @Singleton
    fun provideWeatherAlertsRepository(
        repository: QWeatherWeatherRepository,
    ): WeatherAlertsRepository = repository

    @Provides
    @Singleton
    fun provideAirQualityRepository(
        repository: QWeatherWeatherRepository,
    ): AirQualityRepository = repository

    @Provides
    @Singleton
    fun provideMinutePrecipitationRepository(
        repository: QWeatherWeatherRepository,
    ): MinutePrecipitationRepository = repository

    @Provides
    @Singleton
    fun provideSunriseSunsetRepository(
        repository: QWeatherWeatherRepository,
    ): SunriseSunsetRepository = repository

    @Provides
    @Singleton
    fun provideWeatherIndicesRepository(
        repository: QWeatherWeatherRepository,
    ): WeatherIndicesRepository = repository
}
