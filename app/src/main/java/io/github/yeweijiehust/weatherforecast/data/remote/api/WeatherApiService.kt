package io.github.yeweijiehust.weatherforecast.data.remote.api

import io.github.yeweijiehust.weatherforecast.data.remote.dto.CurrentWeatherResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.HourlyForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v7/weather/now")
    suspend fun getCurrentWeather(
        @Query("location") locationId: String,
        @Query("lang") language: String,
        @Query("unit") unit: String,
    ): CurrentWeatherResponseDto

    @GET("v7/weather/24h")
    suspend fun getHourlyForecast(
        @Query("location") locationId: String,
        @Query("lang") language: String,
        @Query("unit") unit: String,
    ): HourlyForecastResponseDto
}
