package io.github.yeweijiehust.weatherforecast.data.repository

import io.github.yeweijiehust.weatherforecast.data.remote.api.WeatherApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomain
import io.github.yeweijiehust.weatherforecast.data.remote.mapper.toDomainOrNull
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.AirQualityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.MinutePrecipitationRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SunriseSunsetRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherAlertsRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherIndicesRepository
import java.net.SocketTimeoutException
import retrofit2.HttpException

internal class QWeatherSecondaryRepository(
    private val weatherApiService: WeatherApiService,
    private val qWeatherConfig: QWeatherConfig,
    private val settingsRepository: SettingsRepository,
    private val policyStore: WeatherRequestPolicyStore,
) : WeatherAlertsRepository,
    AirQualityRepository,
    MinutePrecipitationRepository,
    SunriseSunsetRepository,
    WeatherIndicesRepository {
    private val policyLock = Any()
    private val weatherAlertCache = mutableMapOf<String, CachedResult<WeatherAlertFetchResult>>()
    private val airQualityCache = mutableMapOf<String, CachedResult<AirQualityFetchResult>>()
    private val minutePrecipitationCache = mutableMapOf<String, CachedResult<MinutePrecipitationFetchResult>>()
    private val sunriseSunsetCache = mutableMapOf<String, CachedResult<SunriseSunsetFetchResult>>()
    private val weatherIndicesCache = mutableMapOf<String, CachedResult<WeatherIndicesFetchResult>>()

    override suspend fun fetchWeatherAlerts(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean,
    ): WeatherAlertFetchResult {
        check(qWeatherConfig.isConfigured) {
            "Weather API is not configured. Add api_key and api_host to local.properties."
        }

        val settings = settingsRepository.getCurrentSettings()
        val settingsSignature = policyStore.settingsSignature(
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val cacheKey = policyStore.buildPolicyKey(
            dataset = "alerts-cache",
            locationKey = "$latitude,$longitude",
            settingsSignature = settingsSignature,
        )
        val policyKey = policyStore.buildPolicyKey(
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
            policyStore.clearFailureGate(policyKey)
            result
        } catch (error: Throwable) {
            policyStore.recordFailure(policyKey = policyKey, now = now)
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
        val settingsSignature = policyStore.settingsSignature(
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val cacheKey = policyStore.buildPolicyKey(
            dataset = "air-quality-cache",
            locationKey = "$latitude,$longitude",
            settingsSignature = settingsSignature,
        )
        val policyKey = policyStore.buildPolicyKey(
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
                policyStore.recordFailure(policyKey = policyKey, now = now)
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
                policyStore.clearFailureGate(policyKey)
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
        val settingsSignature = policyStore.settingsSignature(
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val cacheKey = policyStore.buildPolicyKey(
            dataset = "minute-precipitation-cache",
            locationKey = "$latitude,$longitude",
            settingsSignature = settingsSignature,
        )
        val policyKey = policyStore.buildPolicyKey(
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
                policyStore.recordFailure(policyKey = policyKey, now = now)
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
                policyStore.clearFailureGate(policyKey)
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
        val settingsSignature = policyStore.settingsSignature(
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val cacheKey = policyStore.buildPolicyKey(
            dataset = "sunrise-sunset-cache",
            locationKey = "$locationId|$date",
            settingsSignature = settingsSignature,
        )
        val policyKey = policyStore.buildPolicyKey(
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
                policyStore.recordFailure(policyKey = policyKey, now = now)
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
                policyStore.clearFailureGate(policyKey)
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
        val settingsSignature = policyStore.settingsSignature(
            language = settings.language.storageValue,
            unitSystem = settings.unitSystem.storageValue,
        )
        val cacheKey = policyStore.buildPolicyKey(
            dataset = "indices-cache",
            locationKey = locationId,
            settingsSignature = settingsSignature,
        )
        val policyKey = policyStore.buildPolicyKey(
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
                policyStore.recordFailure(policyKey = policyKey, now = now)
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
                policyStore.clearFailureGate(policyKey)
                result
            }
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
        if (
            !forceRefresh &&
            policyStore.shouldSkipAutoRequest(
                policyKey = policyKey,
                hasCachedData = true,
                now = now,
            )
        ) {
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
        private const val ALERTS_TTL_MILLIS = 10 * 60 * 1000L
        private const val AIR_QUALITY_TTL_MILLIS = 15 * 60 * 1000L
        private const val MINUTE_PRECIPITATION_TTL_MILLIS = 5 * 60 * 1000L
        private const val SUNRISE_SUNSET_TTL_MILLIS = 12 * 60 * 60 * 1000L
        private const val WEATHER_INDICES_TTL_MILLIS = 6 * 60 * 60 * 1000L
    }
}
