package io.github.yeweijiehust.weatherforecast.data.repository

import io.github.yeweijiehust.weatherforecast.data.local.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.local.source.CurrentWeatherLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DailyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.HourlyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.remote.api.WeatherApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomainOrNull
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toLocalModel
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.math.min
import retrofit2.HttpException

@OptIn(ExperimentalCoroutinesApi::class)
class QWeatherWeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val qWeatherConfig: QWeatherConfig,
    private val currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
    private val hourlyForecastLocalDataSource: HourlyForecastLocalDataSource,
    private val dailyForecastLocalDataSource: DailyForecastLocalDataSource,
    private val settingsRepository: SettingsRepository,
) : WeatherRepository {
    private val policyLock = Any()
    private val refreshFailureGates = mutableMapOf<String, RefreshFailureGate>()
    private val weatherAlertCache = mutableMapOf<String, CachedResult<WeatherAlertFetchResult>>()
    private val airQualityCache = mutableMapOf<String, CachedResult<AirQualityFetchResult>>()
    private val minutePrecipitationCache = mutableMapOf<String, CachedResult<MinutePrecipitationFetchResult>>()
    private val sunriseSunsetCache = mutableMapOf<String, CachedResult<SunriseSunsetFetchResult>>()
    private val weatherIndicesCache = mutableMapOf<String, CachedResult<WeatherIndicesFetchResult>>()

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
        val policyKey = buildPolicyKey(
            dataset = "current",
            locationKey = cityId,
            settingsSignature = settingsSignature(settings.language.storageValue, settings.unitSystem.storageValue),
        )
        if (!forceRefresh) {
            val cachedFetchedAt = localCurrentWeather?.fetchedAtEpochMillis
            if (isFresh(cachedFetchedAt, CURRENT_WEATHER_TTL_MILLIS, now)) {
                return
            }
            if (shouldSkipAutoRequest(policyKey = policyKey, hasCachedData = localCurrentWeather != null, now = now)) {
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
            clearFailureGate(policyKey)
        } catch (error: Throwable) {
            recordFailure(policyKey = policyKey, now = now)
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
        val policyKey = buildPolicyKey(
            dataset = "hourly",
            locationKey = cityId,
            settingsSignature = settingsSignature(settings.language.storageValue, settings.unitSystem.storageValue),
        )
        if (!forceRefresh) {
            val cachedFetchedAt = localHourlyForecast.maxOfOrNull { localModel ->
                localModel.fetchedAtEpochMillis
            }
            if (isFresh(cachedFetchedAt, HOURLY_FORECAST_TTL_MILLIS, now)) {
                return
            }
            if (shouldSkipAutoRequest(policyKey = policyKey, hasCachedData = localHourlyForecast.isNotEmpty(), now = now)) {
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
            clearFailureGate(policyKey)
        } catch (error: Throwable) {
            recordFailure(policyKey = policyKey, now = now)
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
        val policyKey = buildPolicyKey(
            dataset = "daily",
            locationKey = cityId,
            settingsSignature = settingsSignature(settings.language.storageValue, settings.unitSystem.storageValue),
        )
        if (!forceRefresh) {
            val cachedFetchedAt = localDailyForecast.maxOfOrNull { localModel ->
                localModel.fetchedAtEpochMillis
            }
            if (isFresh(cachedFetchedAt, DAILY_FORECAST_TTL_MILLIS, now)) {
                return
            }
            if (shouldSkipAutoRequest(policyKey = policyKey, hasCachedData = localDailyForecast.isNotEmpty(), now = now)) {
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
            clearFailureGate(policyKey)
        } catch (error: Throwable) {
            recordFailure(policyKey = policyKey, now = now)
            throw error
        }
    }

    override suspend fun fetchWeatherAlerts(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean,
    ): WeatherAlertFetchResult {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val settingsSignature = settingsSignature(
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val cacheKey = buildPolicyKey(
            dataset = "alerts-cache",
            locationKey = "$latitude,$longitude",
            settingsSignature = settingsSignature,
        )
        val policyKey = buildPolicyKey(
            dataset = "alerts",
            locationKey = "$latitude,$longitude",
            settingsSignature = settingsSignature,
        )
        val now = System.currentTimeMillis()
        val cachedResult = getCachedSecondaryResult(
            cache = weatherAlertCache,
            cacheKey = cacheKey,
            ttlMillis = ALERTS_TTL_MILLIS,
            forceRefresh = forceRefresh,
            policyKey = policyKey,
            now = now,
        )
        if (cachedResult != null) {
            return cachedResult
        }

        return try {
            val response = weatherApiService.getWeatherAlerts(
                latitude = latitude,
                longitude = longitude,
                language = settings.language.apiCode,
            )
            val alerts = response.alerts
            val result = if (alerts.isNotEmpty()) {
                WeatherAlertFetchResult.Available(
                    alerts = alerts.map { alertDto -> alertDto.toDomain() },
                )
            } else {
                WeatherAlertFetchResult.Empty
            }
            saveSecondaryResult(
                cache = weatherAlertCache,
                cacheKey = cacheKey,
                value = result,
                now = now,
            )
            clearFailureGate(policyKey)
            result
        } catch (error: Throwable) {
            recordFailure(policyKey = policyKey, now = now)
            getCachedSecondaryResult(
                cache = weatherAlertCache,
                cacheKey = cacheKey,
                ttlMillis = Long.MAX_VALUE,
                forceRefresh = false,
                policyKey = policyKey,
                now = now,
            ) ?: throw error
        }
    }

    override suspend fun fetchAirQuality(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean,
    ): AirQualityFetchResult {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val settingsSignature = settingsSignature(
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val cacheKey = buildPolicyKey(
            dataset = "air-quality-cache",
            locationKey = "$latitude,$longitude",
            settingsSignature = settingsSignature,
        )
        val policyKey = buildPolicyKey(
            dataset = "air-quality",
            locationKey = "$latitude,$longitude",
            settingsSignature = settingsSignature,
        )
        val now = System.currentTimeMillis()
        getCachedSecondaryResult(
            cache = airQualityCache,
            cacheKey = cacheKey,
            ttlMillis = AIR_QUALITY_TTL_MILLIS,
            forceRefresh = forceRefresh,
            policyKey = policyKey,
            now = now,
        )?.let { cached ->
            return cached
        }

        val result = try {
            val response = weatherApiService.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                language = settings.language.apiCode,
            )
            response.toDomainOrNull()?.let { airQuality ->
                AirQualityFetchResult.Available(airQuality)
            } ?: if (
                response.metadata.zeroResult == true ||
                (response.indexes.isEmpty() && response.pollutants.isEmpty())
            ) {
                AirQualityFetchResult.UnsupportedRegion
            } else {
                AirQualityFetchResult.Failure(AirQualityFailureReason.Unknown)
            }
        } catch (_: SocketTimeoutException) {
            AirQualityFetchResult.Failure(AirQualityFailureReason.Timeout)
        } catch (httpException: HttpException) {
            when (httpException.code()) {
                UNAUTHORIZED_STATUS_CODE -> {
                    AirQualityFetchResult.Failure(AirQualityFailureReason.Unauthorized)
                }

                QUOTA_EXCEEDED_STATUS_CODE -> {
                    AirQualityFetchResult.Failure(AirQualityFailureReason.QuotaExceeded)
                }

                else -> {
                    AirQualityFetchResult.Failure(AirQualityFailureReason.Unknown)
                }
            }
        }
        return when (result) {
            is AirQualityFetchResult.Failure -> {
                recordFailure(policyKey = policyKey, now = now)
                getCachedSecondaryResult(
                    cache = airQualityCache,
                    cacheKey = cacheKey,
                    ttlMillis = Long.MAX_VALUE,
                    forceRefresh = false,
                    policyKey = policyKey,
                    now = now,
                ) ?: result
            }

            else -> {
                saveSecondaryResult(
                    cache = airQualityCache,
                    cacheKey = cacheKey,
                    value = result,
                    now = now,
                )
                clearFailureGate(policyKey)
                result
            }
        }
    }

    override suspend fun fetchMinutePrecipitation(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean,
    ): MinutePrecipitationFetchResult {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val settingsSignature = settingsSignature(
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val cacheKey = buildPolicyKey(
            dataset = "minute-precipitation-cache",
            locationKey = "$latitude,$longitude",
            settingsSignature = settingsSignature,
        )
        val policyKey = buildPolicyKey(
            dataset = "minute-precipitation",
            locationKey = "$latitude,$longitude",
            settingsSignature = settingsSignature,
        )
        val now = System.currentTimeMillis()
        getCachedSecondaryResult(
            cache = minutePrecipitationCache,
            cacheKey = cacheKey,
            ttlMillis = MINUTE_PRECIPITATION_TTL_MILLIS,
            forceRefresh = forceRefresh,
            policyKey = policyKey,
            now = now,
        )?.let { cached ->
            return cached
        }

        val result = try {
            val response = weatherApiService.getMinutePrecipitation(
                location = "$longitude,$latitude",
                language = settings.language.apiCode,
            )
            when {
                response.code == SUCCESS_CODE -> {
                    response.toDomainOrNull()?.let { timeline ->
                        MinutePrecipitationFetchResult.Available(timeline = timeline)
                    } ?: MinutePrecipitationFetchResult.UnsupportedRegion
                }

                response.code == NO_DATA_CODE -> {
                    MinutePrecipitationFetchResult.UnsupportedRegion
                }

                else -> {
                    MinutePrecipitationFetchResult.Failure(MinutePrecipitationFailureReason.Unknown)
                }
            }
        } catch (_: SocketTimeoutException) {
            MinutePrecipitationFetchResult.Failure(MinutePrecipitationFailureReason.Timeout)
        } catch (httpException: HttpException) {
            when (httpException.code()) {
                UNAUTHORIZED_STATUS_CODE -> {
                    MinutePrecipitationFetchResult.Failure(
                        MinutePrecipitationFailureReason.Unauthorized,
                    )
                }

                QUOTA_EXCEEDED_STATUS_CODE -> {
                    MinutePrecipitationFetchResult.Failure(
                        MinutePrecipitationFailureReason.QuotaExceeded,
                    )
                }

                else -> {
                    MinutePrecipitationFetchResult.Failure(MinutePrecipitationFailureReason.Unknown)
                }
            }
        }
        return when (result) {
            is MinutePrecipitationFetchResult.Failure -> {
                recordFailure(policyKey = policyKey, now = now)
                getCachedSecondaryResult(
                    cache = minutePrecipitationCache,
                    cacheKey = cacheKey,
                    ttlMillis = Long.MAX_VALUE,
                    forceRefresh = false,
                    policyKey = policyKey,
                    now = now,
                ) ?: result
            }

            else -> {
                saveSecondaryResult(
                    cache = minutePrecipitationCache,
                    cacheKey = cacheKey,
                    value = result,
                    now = now,
                )
                clearFailureGate(policyKey)
                result
            }
        }
    }

    override suspend fun fetchSunriseSunset(
        locationId: String,
        date: String,
        forceRefresh: Boolean,
    ): SunriseSunsetFetchResult {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val settingsSignature = settingsSignature(
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val cacheKey = buildPolicyKey(
            dataset = "sunrise-sunset-cache",
            locationKey = "$locationId|$date",
            settingsSignature = settingsSignature,
        )
        val policyKey = buildPolicyKey(
            dataset = "sunrise-sunset",
            locationKey = "$locationId|$date",
            settingsSignature = settingsSignature,
        )
        val now = System.currentTimeMillis()
        getCachedSecondaryResult(
            cache = sunriseSunsetCache,
            cacheKey = cacheKey,
            ttlMillis = SUNRISE_SUNSET_TTL_MILLIS,
            forceRefresh = forceRefresh,
            policyKey = policyKey,
            now = now,
        )?.let { cached ->
            return cached
        }

        val result = try {
            val response = weatherApiService.getSunriseSunset(
                locationId = locationId,
                date = date,
                language = settings.language.apiCode,
            )
            if (response.code == SUCCESS_CODE) {
                SunriseSunsetFetchResult.Available(response.toDomain())
            } else {
                SunriseSunsetFetchResult.Failure(SunriseSunsetFailureReason.Unknown)
            }
        } catch (_: SocketTimeoutException) {
            SunriseSunsetFetchResult.Failure(SunriseSunsetFailureReason.Timeout)
        } catch (httpException: HttpException) {
            when (httpException.code()) {
                UNAUTHORIZED_STATUS_CODE -> {
                    SunriseSunsetFetchResult.Failure(SunriseSunsetFailureReason.Unauthorized)
                }

                QUOTA_EXCEEDED_STATUS_CODE -> {
                    SunriseSunsetFetchResult.Failure(SunriseSunsetFailureReason.QuotaExceeded)
                }

                else -> {
                    SunriseSunsetFetchResult.Failure(SunriseSunsetFailureReason.Unknown)
                }
            }
        }
        return when (result) {
            is SunriseSunsetFetchResult.Failure -> {
                recordFailure(policyKey = policyKey, now = now)
                getCachedSecondaryResult(
                    cache = sunriseSunsetCache,
                    cacheKey = cacheKey,
                    ttlMillis = Long.MAX_VALUE,
                    forceRefresh = false,
                    policyKey = policyKey,
                    now = now,
                ) ?: result
            }

            else -> {
                saveSecondaryResult(
                    cache = sunriseSunsetCache,
                    cacheKey = cacheKey,
                    value = result,
                    now = now,
                )
                clearFailureGate(policyKey)
                result
            }
        }
    }

    override suspend fun fetchWeatherIndices(
        locationId: String,
        forceRefresh: Boolean,
    ): WeatherIndicesFetchResult {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val settingsSignature = settingsSignature(
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val cacheKey = buildPolicyKey(
            dataset = "indices-cache",
            locationKey = locationId,
            settingsSignature = settingsSignature,
        )
        val policyKey = buildPolicyKey(
            dataset = "indices",
            locationKey = locationId,
            settingsSignature = settingsSignature,
        )
        val now = System.currentTimeMillis()
        getCachedSecondaryResult(
            cache = weatherIndicesCache,
            cacheKey = cacheKey,
            ttlMillis = WEATHER_INDICES_TTL_MILLIS,
            forceRefresh = forceRefresh,
            policyKey = policyKey,
            now = now,
        )?.let { cached ->
            return cached
        }

        val result = try {
            val response = weatherApiService.getWeatherIndices(
                type = INDEX_TYPE_ALL,
                locationId = locationId,
                language = settings.language.apiCode,
            )
            when {
                response.code == SUCCESS_CODE -> {
                    val weatherIndices = response.toDomain()
                    if (weatherIndices.items.isEmpty()) {
                        WeatherIndicesFetchResult.Empty
                    } else {
                        WeatherIndicesFetchResult.Available(weatherIndices = weatherIndices)
                    }
                }

                response.code == NO_DATA_CODE -> {
                    WeatherIndicesFetchResult.Empty
                }

                else -> {
                    WeatherIndicesFetchResult.Failure(WeatherIndicesFailureReason.Unknown)
                }
            }
        } catch (_: SocketTimeoutException) {
            WeatherIndicesFetchResult.Failure(WeatherIndicesFailureReason.Timeout)
        } catch (httpException: HttpException) {
            when (httpException.code()) {
                UNAUTHORIZED_STATUS_CODE -> {
                    WeatherIndicesFetchResult.Failure(WeatherIndicesFailureReason.Unauthorized)
                }

                QUOTA_EXCEEDED_STATUS_CODE -> {
                    WeatherIndicesFetchResult.Failure(WeatherIndicesFailureReason.QuotaExceeded)
                }

                else -> {
                    WeatherIndicesFetchResult.Failure(WeatherIndicesFailureReason.Unknown)
                }
            }
        }
        return when (result) {
            is WeatherIndicesFetchResult.Failure -> {
                recordFailure(policyKey = policyKey, now = now)
                getCachedSecondaryResult(
                    cache = weatherIndicesCache,
                    cacheKey = cacheKey,
                    ttlMillis = Long.MAX_VALUE,
                    forceRefresh = false,
                    policyKey = policyKey,
                    now = now,
                ) ?: result
            }

            else -> {
                saveSecondaryResult(
                    cache = weatherIndicesCache,
                    cacheKey = cacheKey,
                    value = result,
                    now = now,
                )
                clearFailureGate(policyKey)
                result
            }
        }
    }

    private fun settingsSignature(
        language: String,
        unitSystem: String,
    ): String = "$language|$unitSystem"

    private fun buildPolicyKey(
        dataset: String,
        locationKey: String,
        settingsSignature: String,
    ): String = "$dataset::$locationKey::$settingsSignature"

    private fun isFresh(
        fetchedAtMillis: Long?,
        ttlMillis: Long,
        now: Long,
    ): Boolean {
        if (fetchedAtMillis == null) return false
        return now - fetchedAtMillis <= ttlMillis
    }

    private fun shouldSkipAutoRequest(
        policyKey: String,
        hasCachedData: Boolean,
        now: Long,
    ): Boolean {
        if (!hasCachedData) return false
        return synchronized(policyLock) {
            val gate = refreshFailureGates[policyKey] ?: return@synchronized false
            now < gate.nextAllowedAtMillis
        }
    }

    private fun recordFailure(
        policyKey: String,
        now: Long,
    ) {
        synchronized(policyLock) {
            val failureCount = (refreshFailureGates[policyKey]?.failureCount ?: 0) + 1
            val exponent = (failureCount - 1).coerceAtMost(BACKOFF_MAX_EXPONENT)
            val backoffMultiplier = 1L shl exponent
            val backoffDelay = min(
                AUTO_RETRY_BASE_DELAY_MILLIS * backoffMultiplier,
                AUTO_RETRY_MAX_DELAY_MILLIS,
            )
            refreshFailureGates[policyKey] = RefreshFailureGate(
                failureCount = failureCount,
                nextAllowedAtMillis = now + backoffDelay,
            )
        }
    }

    private fun clearFailureGate(policyKey: String) {
        synchronized(policyLock) {
            refreshFailureGates.remove(policyKey)
        }
    }

    private fun <T> getCachedSecondaryResult(
        cache: MutableMap<String, CachedResult<T>>,
        cacheKey: String,
        ttlMillis: Long,
        forceRefresh: Boolean,
        policyKey: String,
        now: Long,
    ): T? {
        val cached = synchronized(policyLock) { cache[cacheKey] } ?: return null
        if (!forceRefresh && now - cached.fetchedAtMillis <= ttlMillis) {
            return cached.value
        }
        if (!forceRefresh && shouldSkipAutoRequest(policyKey = policyKey, hasCachedData = true, now = now)) {
            return cached.value
        }
        return null
    }

    private fun <T> saveSecondaryResult(
        cache: MutableMap<String, CachedResult<T>>,
        cacheKey: String,
        value: T,
        now: Long,
    ) {
        synchronized(policyLock) {
            cache[cacheKey] = CachedResult(
                value = value,
                fetchedAtMillis = now,
            )
        }
    }

    private data class RefreshFailureGate(
        val failureCount: Int,
        val nextAllowedAtMillis: Long,
    )

    private data class CachedResult<T>(
        val value: T,
        val fetchedAtMillis: Long,
    )

    private companion object {
        private const val SUCCESS_CODE = "200"
        private const val NO_DATA_CODE = "204"
        private const val UNAUTHORIZED_STATUS_CODE = 401
        private const val QUOTA_EXCEEDED_STATUS_CODE = 402
        private const val INDEX_TYPE_ALL = "0"
        private const val CURRENT_WEATHER_TTL_MILLIS = 10 * 60 * 1000L
        private const val HOURLY_FORECAST_TTL_MILLIS = 30 * 60 * 1000L
        private const val DAILY_FORECAST_TTL_MILLIS = 3 * 60 * 60 * 1000L
        private const val ALERTS_TTL_MILLIS = 10 * 60 * 1000L
        private const val AIR_QUALITY_TTL_MILLIS = 15 * 60 * 1000L
        private const val MINUTE_PRECIPITATION_TTL_MILLIS = 5 * 60 * 1000L
        private const val SUNRISE_SUNSET_TTL_MILLIS = 12 * 60 * 60 * 1000L
        private const val WEATHER_INDICES_TTL_MILLIS = 6 * 60 * 60 * 1000L
        private const val AUTO_RETRY_BASE_DELAY_MILLIS = 15 * 1000L
        private const val AUTO_RETRY_MAX_DELAY_MILLIS = 10 * 60 * 1000L
        private const val BACKOFF_MAX_EXPONENT = 6
    }
}
