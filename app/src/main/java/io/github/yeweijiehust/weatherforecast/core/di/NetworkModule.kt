package io.github.yeweijiehust.weatherforecast.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.yeweijiehust.weatherforecast.BuildConfig
import io.github.yeweijiehust.weatherforecast.data.remote.api.GeoApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.repository.QWeatherCityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SearchLanguageProvider
import java.util.Locale
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
    fun provideQWeatherConfig(): QWeatherConfig {
        return QWeatherConfig(
            apiKey = BuildConfig.QWEATHER_API_KEY,
            apiHost = BuildConfig.QWEATHER_API_HOST,
        )
    }

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
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
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
    fun provideCityRepository(
        geoApiService: GeoApiService,
        qWeatherConfig: QWeatherConfig,
    ): CityRepository = QWeatherCityRepository(
        geoApiService = geoApiService,
        qWeatherConfig = qWeatherConfig,
    )

    @Provides
    @Singleton
    fun provideSearchLanguageProvider(): SearchLanguageProvider {
        return object : SearchLanguageProvider {
            override fun currentLanguage(): String {
                return Locale.getDefault().language.ifBlank { "en" }
            }
        }
    }
}
