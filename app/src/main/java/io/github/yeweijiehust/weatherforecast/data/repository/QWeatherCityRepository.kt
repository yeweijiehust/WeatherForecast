package io.github.yeweijiehust.weatherforecast.data.repository

import io.github.yeweijiehust.weatherforecast.data.remote.api.GeoApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import javax.inject.Inject

class QWeatherCityRepository @Inject constructor(
    private val geoApiService: GeoApiService,
    private val qWeatherConfig: QWeatherConfig,
) : CityRepository {
    override suspend fun searchCities(
        query: String,
        language: String,
    ): List<City> {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val response = geoApiService.lookupCities(
            location = query,
            language = language,
            number = SEARCH_LIMIT,
        )

        check(response.code == SUCCESS_CODE) {
            "City search failed with code ${response.code}."
        }

        return response.location.map { it.toDomain() }
    }

    private companion object {
        private const val SUCCESS_CODE = "200"
        private const val SEARCH_LIMIT = 10
    }
}
