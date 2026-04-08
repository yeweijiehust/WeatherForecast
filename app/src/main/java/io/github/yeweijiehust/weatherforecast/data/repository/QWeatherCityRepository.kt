package io.github.yeweijiehust.weatherforecast.data.repository

import io.github.yeweijiehust.weatherforecast.data.local.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.local.model.SavedCityLocalModel
import io.github.yeweijiehust.weatherforecast.data.local.source.DefaultCityPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.SavedCityLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.remote.api.GeoApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomain as toRemoteDomain
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.SaveCityResult
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class QWeatherCityRepository @Inject constructor(
    private val geoApiService: GeoApiService,
    private val qWeatherConfig: QWeatherConfig,
    private val savedCityLocalDataSource: SavedCityLocalDataSource,
    private val defaultCityPreferencesDataSource: DefaultCityPreferencesDataSource,
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

        return response.location.map { it.toRemoteDomain() }
    }

    override fun observeSavedCities(): Flow<List<City>> {
        return combine(
            savedCityLocalDataSource.observeSavedCities(),
            defaultCityPreferencesDataSource.observeDefaultCityId(),
        ) { savedCities, defaultCityId ->
            savedCities.map { savedCity ->
                savedCity.toDomain(isDefault = savedCity.locationId == defaultCityId)
            }
        }
    }

    override fun observeDefaultCity(): Flow<City?> {
        return combine(
            savedCityLocalDataSource.observeSavedCities(),
            defaultCityPreferencesDataSource.observeDefaultCityId(),
        ) { savedCities, defaultCityId ->
            val resolvedDefault = savedCities.firstOrNull { it.locationId == defaultCityId }
                ?: savedCities.firstOrNull()
            resolvedDefault?.toDomain(isDefault = true)
        }
    }

    override suspend fun saveCity(city: City): SaveCityResult {
        if (savedCityLocalDataSource.getSavedCity(city.id) != null) {
            return SaveCityResult.Duplicate
        }

        val inserted = savedCityLocalDataSource.insertCity(
            SavedCityLocalModel(
                locationId = city.id,
                name = city.name,
                adm1 = city.adm1,
                adm2 = city.adm2,
                country = city.country,
                lat = city.lat,
                lon = city.lon,
                timeZone = city.timeZone,
                sortOrder = savedCityLocalDataSource.nextSortOrder(),
                createdAtEpochMillis = System.currentTimeMillis(),
            ),
        )

        if (!inserted) {
            return SaveCityResult.Duplicate
        }

        if (defaultCityPreferencesDataSource.getDefaultCityId() == null) {
            defaultCityPreferencesDataSource.setDefaultCityId(city.id)
        }

        return SaveCityResult.Saved
    }

    override suspend fun setDefaultCity(cityId: String) {
        val savedCity = savedCityLocalDataSource.getSavedCity(cityId) ?: return
        defaultCityPreferencesDataSource.setDefaultCityId(savedCity.locationId)
    }

    override suspend fun removeCity(cityId: String) {
        val currentDefaultId = defaultCityPreferencesDataSource.getDefaultCityId()
        savedCityLocalDataSource.deleteCity(cityId)

        if (currentDefaultId == cityId) {
            val nextDefaultId = savedCityLocalDataSource.getSavedCities()
                .minByOrNull(SavedCityLocalModel::sortOrder)
                ?.locationId
            defaultCityPreferencesDataSource.setDefaultCityId(nextDefaultId)
        }
    }

    private companion object {
        private const val SUCCESS_CODE = "200"
        private const val SEARCH_LIMIT = 10
    }
}
