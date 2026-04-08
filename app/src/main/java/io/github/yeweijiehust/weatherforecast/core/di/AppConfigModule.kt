package io.github.yeweijiehust.weatherforecast.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.yeweijiehust.weatherforecast.BuildConfig
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Provides
    @Singleton
    fun provideQWeatherConfig(): QWeatherConfig {
        return QWeatherConfig(
            apiKey = BuildConfig.QWEATHER_API_KEY,
            apiHost = BuildConfig.QWEATHER_API_HOST,
        )
    }
}
