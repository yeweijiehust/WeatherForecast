package io.github.yeweijiehust.weatherforecast.data.repository

import io.github.yeweijiehust.weatherforecast.data.local.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.local.source.CurrentWeatherLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DailyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.HourlyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.remote.api.WeatherApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toLocalModel
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
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
    private val hourlyForecastLocalDataSource: HourlyForecastLocalDataSource,
    private val dailyForecastLocalDataSource: DailyForecastLocalDataSource,
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

    override fun observeHourlyForecast(cityId: String): Flow<List<HourlyForecast>> {
        return settingsRepository.observeAppSettings().flatMapLatest { settings ->
            hourlyForecastLocalDataSource.observeHourlyForecast(
                cityId = cityId,
                language = settings.language.storageValue,
                unitSystem = settings.unitSystem.storageValue,
            )
        }.map { localModels ->
            localModels.map { localModel -> localModel.toDomain() }
        }
    }

    override suspend fun refreshHourlyForecast(cityId: String) {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val response = weatherApiService.getHourlyForecast(
            locationId = cityId,
            language = settings.language.apiCode,
            unit = settings.unitSystem.apiCode,
        )
        check(response.code == SUCCESS_CODE && response.hourly != null) {
            "Hourly forecast request failed with code ${response.code}."
        }

        val fetchedAt = System.currentTimeMillis()
        hourlyForecastLocalDataSource.replaceHourlyForecast(
            cityId = cityId,
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
            hourlyForecast = response.hourly.map { hourlyDto ->
                hourlyDto.toLocalModel(
                    cityId = cityId,
                    fetchedAtEpochMillis = fetchedAt,
                    language = settings.language.storageValue,
                    unitSystem = settings.unitSystem.storageValue,
                )
            },
        )
    }

    override fun observeDailyForecast(cityId: String): Flow<List<DailyForecast>> {
        return settingsRepository.observeAppSettings().flatMapLatest { settings ->
            dailyForecastLocalDataSource.observeDailyForecast(
                cityId = cityId,
                language = settings.language.storageValue,
                unitSystem = settings.unitSystem.storageValue,
            )
        }.map { localModels ->
            localModels.map { localModel -> localModel.toDomain() }
        }
    }

    override suspend fun refreshDailyForecast(cityId: String) {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val response = weatherApiService.getDailyForecast(
            locationId = cityId,
            language = settings.language.apiCode,
            unit = settings.unitSystem.apiCode,
        )
        check(response.code == SUCCESS_CODE && response.daily != null) {
            "Daily forecast request failed with code ${response.code}."
        }

        val fetchedAt = System.currentTimeMillis()
        dailyForecastLocalDataSource.replaceDailyForecast(
            cityId = cityId,
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
            dailyForecast = response.daily.map { dailyDto ->
                dailyDto.toLocalModel(
                    cityId = cityId,
                    fetchedAtEpochMillis = fetchedAt,
                    language = settings.language.storageValue,
                    unitSystem = settings.unitSystem.storageValue,
                )
            },
        )
    }

    override suspend fun fetchWeatherAlerts(
        latitude: String,
        longitude: String,
    ): WeatherAlertFetchResult {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val response = weatherApiService.getWeatherAlerts(
            latitude = latitude,
            longitude = longitude,
            language = settings.language.apiCode,
        )
        return when {
            response.code == SUCCESS_CODE && response.warning.isNotEmpty() -> {
                WeatherAlertFetchResult.Available(
                    alerts = response.warning.map { alertDto -> alertDto.toDomain() },
                )
            }

            response.code == SUCCESS_CODE || response.code == NO_ALERT_CODE -> {
                WeatherAlertFetchResult.Empty
            }

            else -> {
                error("Weather alert request failed with code ${response.code}.")
            }
        }
    }

    private companion object {
        private const val SUCCESS_CODE = "200"
        private const val NO_ALERT_CODE = "204"
    }
}
