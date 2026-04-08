package io.github.yeweijiehust.weatherforecast.domain.repository

import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.SaveCityResult
import kotlinx.coroutines.flow.Flow

interface CityRepository {
    suspend fun searchCities(
        query: String,
        language: String,
    ): List<City>

    fun observeSavedCities(): Flow<List<City>>

    fun observeDefaultCity(): Flow<City?>

    suspend fun saveCity(city: City): SaveCityResult

    suspend fun setDefaultCity(cityId: String)

    suspend fun removeCity(cityId: String)
}
