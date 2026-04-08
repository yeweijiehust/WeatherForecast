package io.github.yeweijiehust.weatherforecast.data.remote.api

import io.github.yeweijiehust.weatherforecast.data.remote.dto.CityLookupResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.TopCityResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoApiService {
    @GET("geo/v2/city/lookup")
    suspend fun lookupCities(
        @Query("location") location: String,
        @Query("lang") language: String,
        @Query("number") number: Int,
    ): CityLookupResponseDto

    @GET("geo/v2/city/top")
    suspend fun topCities(
        @Query("lang") language: String,
        @Query("number") number: Int,
    ): TopCityResponseDto
}
