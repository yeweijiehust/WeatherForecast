package io.github.yeweijiehust.weatherforecast.data.remote.api

import io.github.yeweijiehust.weatherforecast.data.remote.dto.CurrentWeatherResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.DailyForecastResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.HourlyForecastResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.MinutePrecipitationResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.SunriseSunsetResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherIndicesResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
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

    @GET("v7/weather/7d")
    suspend fun getDailyForecast(
        @Query("location") locationId: String,
        @Query("lang") language: String,
        @Query("unit") unit: String,
    ): DailyForecastResponseDto

    @GET("weatheralert/v1/current/{latitude}/{longitude}")
    suspend fun getWeatherAlerts(
        @Path("latitude") latitude: String,
        @Path("longitude") longitude: String,
        @Query("lang") language: String,
    ): WeatherAlertResponseDto

    @GET("airquality/v1/current/{latitude}/{longitude}")
    suspend fun getAirQuality(
        @Path("latitude") latitude: String,
        @Path("longitude") longitude: String,
        @Query("lang") language: String,
    ): AirQualityResponseDto

    @GET("v7/minutely/5m")
    suspend fun getMinutePrecipitation(
        @Query("location") location: String,
        @Query("lang") language: String,
    ): MinutePrecipitationResponseDto

    @GET("v7/astronomy/sun")
    suspend fun getSunriseSunset(
        @Query("location") locationId: String,
        @Query("date") date: String,
        @Query("lang") language: String,
    ): SunriseSunsetResponseDto

    @GET("v7/indices/1d")
    suspend fun getWeatherIndices(
        @Query("type") type: String,
        @Query("location") locationId: String,
        @Query("lang") language: String,
    ): WeatherIndicesResponseDto
}
