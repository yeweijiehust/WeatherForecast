package io.github.yeweijiehust.weatherforecast.domain.repository

import io.github.yeweijiehust.weatherforecast.domain.model.City

interface CityRepository {
    suspend fun searchCities(
        query: String,
        language: String,
    ): List<City>
}
