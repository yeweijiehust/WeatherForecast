package io.github.yeweijiehust.weatherforecast.data.repository

import io.github.yeweijiehust.weatherforecast.data.local.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.local.source.CurrentWeatherLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.remote.api.WeatherApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toLocalModel
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class QWeatherWeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val qWeatherConfig: QWeatherConfig,
    private val currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
    private val settingsRepository: SettingsRepository,
) : WeatherRepository {
    override fun observeCurrentWeather(cityId: String): Flow<CurrentWeather?> {
        return settingsRepository.observeAppSettings().flatMapLatest { settings ->
            currentWeatherLocalDataSource.observeCurrentWeather(
                cityId = cityId,
                language = settings.language.storageValue,
                unitSystem = settings.unitSystem.storageValue,
            )
        }.map { localModel ->
            localModel?.toDomain()
        }
    }

    override suspend fun refreshCurrentWeather(cityId: String) {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val response = weatherApiService.getCurrentWeather(
            locationId = cityId,
            language = settings.language.apiCode,
            unit = settings.unitSystem.apiCode,
        )
        check(response.code == SUCCESS_CODE && response.now != null) {
            "Current weather request failed with code ${response.code}."
        }

        currentWeatherLocalDataSource.upsertCurrentWeather(
            response.now.toLocalModel(
                cityId = cityId,
                fetchedAtEpochMillis = System.currentTimeMillis(),
                language = settings.language.storageValue,
                unitSystem = settings.unitSystem.storageValue,
            ),
        )
    }

    private companion object {
        private const val SUCCESS_CODE = "200"
    }
}
