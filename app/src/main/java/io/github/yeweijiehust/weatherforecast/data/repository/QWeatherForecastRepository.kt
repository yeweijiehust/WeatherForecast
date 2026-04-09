package io.github.yeweijiehust.weatherforecast.data.repository

import io.github.yeweijiehust.weatherforecast.data.local.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.local.source.CurrentWeatherLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DailyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.HourlyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.remote.api.WeatherApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toLocalModel
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.repository.CurrentWeatherRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.DailyForecastRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.HourlyForecastRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
internal class QWeatherForecastRepository(
    private val weatherApiService: WeatherApiService,
    private val qWeatherConfig: QWeatherConfig,
    private val currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
    private val hourlyForecastLocalDataSource: HourlyForecastLocalDataSource,
    private val dailyForecastLocalDataSource: DailyForecastLocalDataSource,
    private val settingsRepository: SettingsRepository,
    private val policyStore: WeatherRequestPolicyStore,
) : CurrentWeatherRepository, HourlyForecastRepository, DailyForecastRepository {
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

    override suspend fun refreshCurrentWeather(
        cityId: String,
        forceRefresh: Boolean,
    ) {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val now = System.currentTimeMillis()
        val localCurrentWeather = currentWeatherLocalDataSource.getCurrentWeather(
            cityId = cityId,
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val policyKey = policyStore.buildPolicyKey(
            dataset = "current",
            locationKey = cityId,
            settingsSignature = policyStore.settingsSignature(
                settings.language.storageValue,
                settings.unitSystem.storageValue,
            ),
        )
        if (!forceRefresh) {
            val cachedFetchedAt = localCurrentWeather?.fetchedAtEpochMillis
            if (policyStore.isFresh(cachedFetchedAt, CURRENT_WEATHER_TTL_MILLIS, now)) {
                return
            }
            if (
                policyStore.shouldSkipAutoRequest(
                    policyKey = policyKey,
                    hasCachedData = localCurrentWeather != null,
                    now = now,
                )
            ) {
                return
            }
        }
        try {
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
            policyStore.clearFailureGate(policyKey)
        } catch (error: Throwable) {
            policyStore.recordFailure(policyKey = policyKey, now = now)
            throw error
        }
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

    override suspend fun refreshHourlyForecast(
        cityId: String,
        forceRefresh: Boolean,
    ) {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val now = System.currentTimeMillis()
        val localHourlyForecast = hourlyForecastLocalDataSource.getHourlyForecast(
            cityId = cityId,
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val policyKey = policyStore.buildPolicyKey(
            dataset = "hourly",
            locationKey = cityId,
            settingsSignature = policyStore.settingsSignature(
                settings.language.storageValue,
                settings.unitSystem.storageValue,
            ),
        )
        if (!forceRefresh) {
            val cachedFetchedAt = localHourlyForecast.maxOfOrNull { localModel ->
                localModel.fetchedAtEpochMillis
            }
            if (policyStore.isFresh(cachedFetchedAt, HOURLY_FORECAST_TTL_MILLIS, now)) {
                return
            }
            if (
                policyStore.shouldSkipAutoRequest(
                    policyKey = policyKey,
                    hasCachedData = localHourlyForecast.isNotEmpty(),
                    now = now,
                )
            ) {
                return
            }
        }

        try {
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
            policyStore.clearFailureGate(policyKey)
        } catch (error: Throwable) {
            policyStore.recordFailure(policyKey = policyKey, now = now)
            throw error
        }
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

    override suspend fun refreshDailyForecast(
        cityId: String,
        forceRefresh: Boolean,
    ) {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val now = System.currentTimeMillis()
        val localDailyForecast = dailyForecastLocalDataSource.getDailyForecast(
            cityId = cityId,
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val policyKey = policyStore.buildPolicyKey(
            dataset = "daily",
            locationKey = cityId,
            settingsSignature = policyStore.settingsSignature(
                settings.language.storageValue,
                settings.unitSystem.storageValue,
            ),
        )
        if (!forceRefresh) {
            val cachedFetchedAt = localDailyForecast.maxOfOrNull { localModel ->
                localModel.fetchedAtEpochMillis
            }
            if (policyStore.isFresh(cachedFetchedAt, DAILY_FORECAST_TTL_MILLIS, now)) {
                return
            }
            if (
                policyStore.shouldSkipAutoRequest(
                    policyKey = policyKey,
                    hasCachedData = localDailyForecast.isNotEmpty(),
                    now = now,
                )
            ) {
                return
            }
        }

        try {
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
            policyStore.clearFailureGate(policyKey)
        } catch (error: Throwable) {
            policyStore.recordFailure(policyKey = policyKey, now = now)
            throw error
        }
    }

    private companion object {
        private const val SUCCESS_CODE = "200"
        private const val CURRENT_WEATHER_TTL_MILLIS = 10 * 60 * 1000L
        private const val HOURLY_FORECAST_TTL_MILLIS = 30 * 60 * 1000L
        private const val DAILY_FORECAST_TTL_MILLIS = 3 * 60 * 60 * 1000L
    }
}
