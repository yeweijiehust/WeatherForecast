package io.github.yeweijiehust.weatherforecast.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.yeweijiehust.weatherforecast.data.remote.api.GeoApiService
import io.github.yeweijiehust.weatherforecast.data.remote.api.WeatherApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        qWeatherConfig: QWeatherConfig,
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .apply {
                        if (qWeatherConfig.apiKey.isNotBlank()) {
                            header("X-QW-Api-Key", qWeatherConfig.apiKey)
                        }
                    }
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        qWeatherConfig: QWeatherConfig,
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(qWeatherConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType()),
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideGeoApiService(
        retrofit: Retrofit,
    ): GeoApiService = retrofit.create(GeoApiService::class.java)

    @Provides
    @Singleton
    fun provideWeatherApiService(
        retrofit: Retrofit,
    ): WeatherApiService = retrofit.create(WeatherApiService::class.java)
}
