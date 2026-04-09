package io.github.yeweijiehust.weatherforecast.data.repository

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.local.model.CurrentWeatherLocalModel
import io.github.yeweijiehust.weatherforecast.data.local.model.DailyForecastLocalModel
import io.github.yeweijiehust.weatherforecast.data.local.model.HourlyForecastLocalModel
import io.github.yeweijiehust.weatherforecast.data.local.source.CurrentWeatherLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.DailyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.HourlyForecastLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.remote.api.WeatherApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.remote.dto.CurrentWeatherDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.CurrentWeatherResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityConcentrationDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityMetadataDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.DailyForecastDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.DailyForecastResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityIndexDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityPollutantDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityPrimaryPollutantDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.HourlyForecastDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.HourlyForecastResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.MinutePrecipitationPointDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.MinutePrecipitationResponseDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertEventTypeDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertMetadataDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertResponseDto
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.net.SocketTimeoutException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class QWeatherWeatherRepositoryTest {
    @Test
    fun fetchAirQuality_requestsUsingLatLonAndLanguage() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getAirQuality(
                latitude = "31.23",
                longitude = "121.47",
                language = "zh",
            )
        } returns AirQualityResponseDto(
            metadata = AirQualityMetadataDto(tag = "test-tag"),
            indexes = listOf(
                AirQualityIndexDto(
                    aqi = JsonPrimitive(86),
                    aqiDisplay = "86",
                    category = "Moderate",
                    primaryPollutant = AirQualityPrimaryPollutantDto(code = "pm2p5"),
                ),
            ),
            pollutants = listOf(
                AirQualityPollutantDto(
                    code = "pm2p5",
                    concentration = AirQualityConcentrationDto(value = JsonPrimitive(65)),
                ),
            ),
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.SimplifiedChinese,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val result = repository.fetchAirQuality(latitude = "31.23", longitude = "121.47")

        coVerify(exactly = 1) {
            weatherApiService.getAirQuality(
                latitude = "31.23",
                longitude = "121.47",
                language = "zh",
            )
        }
        assertThat(result).isInstanceOf(AirQualityFetchResult.Available::class.java)
        val available = result as AirQualityFetchResult.Available
        assertThat(available.airQuality.aqi).isEqualTo("86")
        assertThat(available.airQuality.category).isEqualTo("Moderate")
    }

    @Test
    fun fetchAirQuality_returnsUnsupportedRegionWhenZeroResult() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getAirQuality(
                latitude = "31.23",
                longitude = "121.47",
                language = "en",
            )
        } returns AirQualityResponseDto(
            metadata = AirQualityMetadataDto(
                tag = "test-tag",
                zeroResult = true,
            ),
            indexes = emptyList(),
            pollutants = emptyList(),
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val result = repository.fetchAirQuality(latitude = "31.23", longitude = "121.47")

        assertThat(result).isEqualTo(AirQualityFetchResult.UnsupportedRegion)
    }

    @Test
    fun fetchAirQuality_supportsCurrentApiSchema() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getAirQuality(
                latitude = "31.23",
                longitude = "121.47",
                language = "en",
            )
        } returns AirQualityResponseDto(
            metadata = AirQualityMetadataDto(tag = "test-tag"),
            indexes = listOf(
                AirQualityIndexDto(
                    aqi = JsonPrimitive(53),
                    aqiDisplay = "53",
                    category = "Good",
                    primaryPollutant = AirQualityPrimaryPollutantDto(
                        code = "pm2p5",
                        name = "PM 2.5",
                    ),
                ),
            ),
            pollutants = listOf(
                AirQualityPollutantDto(
                    code = "pm2p5",
                    concentration = AirQualityConcentrationDto(value = JsonPrimitive(37.0)),
                ),
            ),
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val result = repository.fetchAirQuality(latitude = "31.23", longitude = "121.47")

        assertThat(result).isInstanceOf(AirQualityFetchResult.Available::class.java)
        val available = result as AirQualityFetchResult.Available
        assertThat(available.airQuality.aqi).isEqualTo("53")
        assertThat(available.airQuality.category).isEqualTo("Good")
        assertThat(available.airQuality.pm2p5).isEqualTo("37.0")
    }

    @Test
    fun fetchAirQuality_mapsUnauthorizedHttpStatusToFailure() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getAirQuality(
                latitude = "31.23",
                longitude = "121.47",
                language = "en",
            )
        } throws httpException(statusCode = 401)
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val result = repository.fetchAirQuality(latitude = "31.23", longitude = "121.47")

        assertThat(result).isEqualTo(
            AirQualityFetchResult.Failure(reason = AirQualityFailureReason.Unauthorized),
        )
    }

    @Test
    fun fetchAirQuality_mapsQuotaHttpStatusToFailure() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getAirQuality(
                latitude = "31.23",
                longitude = "121.47",
                language = "en",
            )
        } throws httpException(statusCode = 402)
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val result = repository.fetchAirQuality(latitude = "31.23", longitude = "121.47")

        assertThat(result).isEqualTo(
            AirQualityFetchResult.Failure(reason = AirQualityFailureReason.QuotaExceeded),
        )
    }

    @Test
    fun fetchAirQuality_mapsTimeoutToFailure() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getAirQuality(
                latitude = "31.23",
                longitude = "121.47",
                language = "en",
            )
        } throws SocketTimeoutException("timeout")
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val result = repository.fetchAirQuality(latitude = "31.23", longitude = "121.47")

        assertThat(result).isEqualTo(
            AirQualityFetchResult.Failure(reason = AirQualityFailureReason.Timeout),
        )
    }

    @Test
    fun fetchMinutePrecipitation_requestsUsingLonLatAndLanguage() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getMinutePrecipitation(
                location = "121.47,31.23",
                language = "zh",
            )
        } returns MinutePrecipitationResponseDto(
            code = "200",
            summary = "Rain expected in about 35 minutes.",
            updateTime = "2026-04-09T14:00+08:00",
            minutely = listOf(
                MinutePrecipitationPointDto(
                    forecastTime = "2026-04-09T14:05+08:00",
                    precipitation = "0.0",
                    type = "rain",
                ),
            ),
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.SimplifiedChinese,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val result = repository.fetchMinutePrecipitation(latitude = "31.23", longitude = "121.47")

        coVerify(exactly = 1) {
            weatherApiService.getMinutePrecipitation(
                location = "121.47,31.23",
                language = "zh",
            )
        }
        assertThat(result).isInstanceOf(MinutePrecipitationFetchResult.Available::class.java)
        val available = result as MinutePrecipitationFetchResult.Available
        assertThat(available.timeline.summary).isEqualTo("Rain expected in about 35 minutes.")
        assertThat(available.timeline.points).hasSize(1)
    }

    @Test
    fun fetchMinutePrecipitation_returnsUnsupportedRegionWhenNoDataCode() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getMinutePrecipitation(
                location = "121.47,31.23",
                language = "en",
            )
        } returns MinutePrecipitationResponseDto(
            code = "204",
            summary = "",
            updateTime = "2026-04-09T14:00+08:00",
            minutely = emptyList(),
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val result = repository.fetchMinutePrecipitation(latitude = "31.23", longitude = "121.47")

        assertThat(result).isEqualTo(MinutePrecipitationFetchResult.UnsupportedRegion)
    }

    @Test
    fun fetchMinutePrecipitation_mapsUnauthorizedHttpStatusToFailure() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getMinutePrecipitation(
                location = "121.47,31.23",
                language = "en",
            )
        } throws httpException(statusCode = 401)
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val result = repository.fetchMinutePrecipitation(latitude = "31.23", longitude = "121.47")

        assertThat(result).isEqualTo(
            MinutePrecipitationFetchResult.Failure(
                reason = MinutePrecipitationFailureReason.Unauthorized,
            ),
        )
    }

    @Test
    fun fetchWeatherAlerts_requestsUsingLatLonAndLanguage() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getWeatherAlerts(
                latitude = "31.23",
                longitude = "121.47",
                language = "zh",
            )
        } returns WeatherAlertResponseDto(
            metadata = WeatherAlertMetadataDto(
                tag = "test-tag",
                zeroResult = false,
            ),
            alerts = listOf(
                WeatherAlertDto(
                    id = "52f63dbf40f5f089f5f69f2d7f929f4f",
                    senderName = "Shanghai Meteorological Center",
                    issuedTime = "2026-04-08T12:00+08:00",
                    headline = "Rainstorm Blue Warning",
                    onsetTime = "2026-04-08T12:00+08:00",
                    expireTime = "2026-04-08T23:00+08:00",
                    status = "active",
                    eventType = WeatherAlertEventTypeDto(
                        code = "rainstorm",
                        name = "Rainstorm",
                    ),
                    severity = "Blue",
                    description = "Expect heavy rain in the next 6 hours.",
                ),
            ),
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.SimplifiedChinese,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val result = repository.fetchWeatherAlerts(latitude = "31.23", longitude = "121.47")

        coVerify(exactly = 1) {
            weatherApiService.getWeatherAlerts(
                latitude = "31.23",
                longitude = "121.47",
                language = "zh",
            )
        }
        assertThat(result).isInstanceOf(WeatherAlertFetchResult.Available::class.java)
        val available = result as WeatherAlertFetchResult.Available
        assertThat(available.alerts).hasSize(1)
        assertThat(available.alerts.single().title).isEqualTo("Rainstorm Blue Warning")
    }

    @Test
    fun fetchWeatherAlerts_returnsEmptyWhenNoActiveAlert() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getWeatherAlerts(
                latitude = "31.23",
                longitude = "121.47",
                language = "en",
            )
        } returns WeatherAlertResponseDto(
            metadata = WeatherAlertMetadataDto(
                tag = "test-tag",
                zeroResult = true,
            ),
            alerts = emptyList(),
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val result = repository.fetchWeatherAlerts(latitude = "31.23", longitude = "121.47")

        assertThat(result).isEqualTo(WeatherAlertFetchResult.Empty)
    }

    @Test
    fun fetchWeatherAlerts_throwsWhenApiReturnsHttpFailure() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        coEvery {
            weatherApiService.getWeatherAlerts(
                latitude = "31.23",
                longitude = "121.47",
                language = "en",
            )
        } throws httpException(statusCode = 401)
        val repository = createRepository(
            weatherApiService = weatherApiService,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val error = runCatching {
            repository.fetchWeatherAlerts(latitude = "31.23", longitude = "121.47")
        }.exceptionOrNull()

        assertThat(error).isNotNull()
        assertThat(error).isInstanceOf(HttpException::class.java)
    }

    @Test
    fun observeCurrentWeather_usesSettingsCompatibleCache() = runTest {
        val localDataSource = FakeCurrentWeatherLocalDataSource(
            currentWeather = sampleCurrentWeatherLocal(),
        )
        val repository = createRepository(
            currentWeatherLocalDataSource = localDataSource,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val observed = repository.observeCurrentWeather("101020100").first()

        assertThat(observed?.cityId).isEqualTo("101020100")
        assertThat(observed?.conditionText).isEqualTo("Sunny")
        assertThat(localDataSource.lastObservedLanguage).isEqualTo("en")
        assertThat(localDataSource.lastObservedUnitSystem).isEqualTo("metric")
    }

    @Test
    fun observeHourlyForecast_usesSettingsCompatibleCache() = runTest {
        val localDataSource = FakeHourlyForecastLocalDataSource(
            hourlyForecast = listOf(sampleHourlyForecastLocal()),
        )
        val repository = createRepository(
            hourlyForecastLocalDataSource = localDataSource,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val observed = repository.observeHourlyForecast("101020100").first()

        assertThat(observed).hasSize(1)
        assertThat(observed.first().cityId).isEqualTo("101020100")
        assertThat(observed.first().conditionText).isEqualTo("Cloudy")
        assertThat(localDataSource.lastObservedLanguage).isEqualTo("en")
        assertThat(localDataSource.lastObservedUnitSystem).isEqualTo("metric")
    }

    @Test
    fun observeDailyForecast_usesSettingsCompatibleCache() = runTest {
        val localDataSource = FakeDailyForecastLocalDataSource(
            dailyForecast = listOf(sampleDailyForecastLocal()),
        )
        val repository = createRepository(
            dailyForecastLocalDataSource = localDataSource,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val observed = repository.observeDailyForecast("101020100").first()

        assertThat(observed).hasSize(1)
        assertThat(observed.first().cityId).isEqualTo("101020100")
        assertThat(observed.first().conditionTextDay).isEqualTo("Sunny")
        assertThat(localDataSource.lastObservedLanguage).isEqualTo("en")
        assertThat(localDataSource.lastObservedUnitSystem).isEqualTo("metric")
    }

    @Test
    fun observeCurrentWeather_returnsCachedDataThenUpdatesAfterRefresh() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        val localDataSource = FakeCurrentWeatherLocalDataSource(
            currentWeather = sampleCurrentWeatherLocal().copy(temperature = "26"),
        )
        coEvery {
            weatherApiService.getCurrentWeather(
                locationId = "101020100",
                language = "en",
                unit = "m",
            )
        } returns CurrentWeatherResponseDto(
            code = "200",
            now = CurrentWeatherDto(
                observationTime = "2026-04-08T14:15+08:00",
                temperature = "29",
                feelsLike = "31",
                conditionText = "Sunny",
                conditionIcon = "100",
                humidity = "60",
                windDirection = "East",
                windScale = "3",
                windSpeed = "10",
                precipitation = "0.0",
                pressure = "1011",
                visibility = "16",
            ),
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            currentWeatherLocalDataSource = localDataSource,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val cached = repository.observeCurrentWeather("101020100").first()
        repository.refreshCurrentWeather("101020100")
        val refreshed = repository.observeCurrentWeather("101020100").first()

        assertThat(cached?.temperature).isEqualTo("26")
        assertThat(refreshed?.temperature).isEqualTo("29")
    }

    @Test
    fun refreshCurrentWeather_requestsUsingCurrentSettingsAndCachesResult() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        val localDataSource = FakeCurrentWeatherLocalDataSource()
        coEvery {
            weatherApiService.getCurrentWeather(
                locationId = "101020100",
                language = "zh",
                unit = "i",
            )
        } returns CurrentWeatherResponseDto(
            code = "200",
            now = CurrentWeatherDto(
                observationTime = "2026-04-08T13:45+08:00",
                temperature = "79",
                feelsLike = "81",
                conditionText = "Sunny",
                conditionIcon = "100",
                humidity = "65",
                windDirection = "East",
                windScale = "3",
                windSpeed = "9",
                precipitation = "0.0",
                pressure = "1012",
                visibility = "10",
            ),
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            currentWeatherLocalDataSource = localDataSource,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.SimplifiedChinese,
                    unitSystem = UnitSystem.Imperial,
                ),
            ),
        )

        repository.refreshCurrentWeather("101020100")

        coVerify(exactly = 1) {
            weatherApiService.getCurrentWeather(
                locationId = "101020100",
                language = "zh",
                unit = "i",
            )
        }
        assertThat(localDataSource.upsertedWeather?.unitSystem).isEqualTo("imperial")
        assertThat(localDataSource.upsertedWeather?.language).isEqualTo("zh")
        assertThat(localDataSource.upsertedWeather?.temperature).isEqualTo("79")
    }

    @Test
    fun refreshHourlyForecast_requestsUsingCurrentSettingsAndCachesResult() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        val localDataSource = FakeHourlyForecastLocalDataSource()
        coEvery {
            weatherApiService.getHourlyForecast(
                locationId = "101020100",
                language = "zh",
                unit = "i",
            )
        } returns HourlyForecastResponseDto(
            code = "200",
            hourly = listOf(
                HourlyForecastDto(
                    forecastTime = "2026-04-08T16:00+08:00",
                    temperature = "82",
                    conditionText = "Cloudy",
                    conditionIcon = "101",
                    precipitationProbability = "20",
                    precipitation = "0.0",
                    windDirection = "South",
                    windScale = "2",
                    windSpeed = "8",
                ),
            ),
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            hourlyForecastLocalDataSource = localDataSource,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.SimplifiedChinese,
                    unitSystem = UnitSystem.Imperial,
                ),
            ),
        )

        repository.refreshHourlyForecast("101020100")

        coVerify(exactly = 1) {
            weatherApiService.getHourlyForecast(
                locationId = "101020100",
                language = "zh",
                unit = "i",
            )
        }
        assertThat(localDataSource.lastReplaceLanguage).isEqualTo("zh")
        assertThat(localDataSource.lastReplaceUnitSystem).isEqualTo("imperial")
        assertThat(localDataSource.replacedHourlyForecast).hasSize(1)
        assertThat(localDataSource.replacedHourlyForecast?.first()?.temperature).isEqualTo("82")
    }

    @Test
    fun refreshHourlyForecast_keepsCachedDataWhenRequestFails() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        val localDataSource = FakeHourlyForecastLocalDataSource(
            hourlyForecast = listOf(sampleHourlyForecastLocal()),
        )
        coEvery {
            weatherApiService.getHourlyForecast(
                locationId = "101020100",
                language = "en",
                unit = "m",
            )
        } returns HourlyForecastResponseDto(
            code = "500",
            hourly = null,
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            hourlyForecastLocalDataSource = localDataSource,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val failure = runCatching {
            repository.refreshHourlyForecast("101020100")
        }.exceptionOrNull()
        val observed = repository.observeHourlyForecast("101020100").first()

        assertThat(failure).isInstanceOf(IllegalStateException::class.java)
        assertThat(observed).hasSize(1)
        assertThat(observed.first().temperature).isEqualTo("24")
    }

    @Test
    fun refreshDailyForecast_requestsUsingCurrentSettingsAndCachesResult() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        val localDataSource = FakeDailyForecastLocalDataSource()
        coEvery {
            weatherApiService.getDailyForecast(
                locationId = "101020100",
                language = "zh",
                unit = "i",
            )
        } returns DailyForecastResponseDto(
            code = "200",
            daily = listOf(
                DailyForecastDto(
                    forecastDate = "2026-04-09",
                    tempMax = "86",
                    tempMin = "72",
                    conditionTextDay = "Sunny",
                    conditionIconDay = "100",
                    precipitation = "0.0",
                    windDirectionDay = "South",
                    windScaleDay = "3",
                    windSpeedDay = "10",
                ),
            ),
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            dailyForecastLocalDataSource = localDataSource,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.SimplifiedChinese,
                    unitSystem = UnitSystem.Imperial,
                ),
            ),
        )

        repository.refreshDailyForecast("101020100")

        coVerify(exactly = 1) {
            weatherApiService.getDailyForecast(
                locationId = "101020100",
                language = "zh",
                unit = "i",
            )
        }
        assertThat(localDataSource.lastReplaceLanguage).isEqualTo("zh")
        assertThat(localDataSource.lastReplaceUnitSystem).isEqualTo("imperial")
        assertThat(localDataSource.replacedDailyForecast).hasSize(1)
        assertThat(localDataSource.replacedDailyForecast?.first()?.tempMax).isEqualTo("86")
    }

    @Test
    fun refreshDailyForecast_keepsCachedDataWhenRequestFails() = runTest {
        val weatherApiService = mockk<WeatherApiService>()
        val localDataSource = FakeDailyForecastLocalDataSource(
            dailyForecast = listOf(sampleDailyForecastLocal()),
        )
        coEvery {
            weatherApiService.getDailyForecast(
                locationId = "101020100",
                language = "en",
                unit = "m",
            )
        } returns DailyForecastResponseDto(
            code = "500",
            daily = null,
        )
        val repository = createRepository(
            weatherApiService = weatherApiService,
            dailyForecastLocalDataSource = localDataSource,
            settingsRepository = FakeSettingsRepository(
                AppSettings(
                    language = AppLanguage.English,
                    unitSystem = UnitSystem.Metric,
                ),
            ),
        )

        val failure = runCatching {
            repository.refreshDailyForecast("101020100")
        }.exceptionOrNull()
        val observed = repository.observeDailyForecast("101020100").first()

        assertThat(failure).isInstanceOf(IllegalStateException::class.java)
        assertThat(observed).hasSize(1)
        assertThat(observed.first().tempMax).isEqualTo("30")
    }

    @Test
    fun refreshCurrentWeather_throwsWhenApiConfigMissing() = runTest {
        val repository = QWeatherWeatherRepository(
            weatherApiService = mockk(),
            qWeatherConfig = QWeatherConfig(apiKey = "", apiHost = ""),
            currentWeatherLocalDataSource = FakeCurrentWeatherLocalDataSource(),
            hourlyForecastLocalDataSource = FakeHourlyForecastLocalDataSource(),
            dailyForecastLocalDataSource = FakeDailyForecastLocalDataSource(),
            settingsRepository = FakeSettingsRepository(),
        )

        val failure = runCatching {
            repository.refreshCurrentWeather("101020100")
        }.exceptionOrNull()

        assertThat(failure).isInstanceOf(IllegalStateException::class.java)
        assertThat(failure).hasMessageThat().contains("Weather API is not configured")
    }

    private fun createRepository(
        weatherApiService: WeatherApiService = mockk(),
        currentWeatherLocalDataSource: CurrentWeatherLocalDataSource = FakeCurrentWeatherLocalDataSource(),
        hourlyForecastLocalDataSource: HourlyForecastLocalDataSource = FakeHourlyForecastLocalDataSource(),
        dailyForecastLocalDataSource: DailyForecastLocalDataSource = FakeDailyForecastLocalDataSource(),
        settingsRepository: SettingsRepository = FakeSettingsRepository(),
    ): QWeatherWeatherRepository {
        return QWeatherWeatherRepository(
            weatherApiService = weatherApiService,
            qWeatherConfig = QWeatherConfig(
                apiKey = "test-key",
                apiHost = "example.com",
            ),
            currentWeatherLocalDataSource = currentWeatherLocalDataSource,
            hourlyForecastLocalDataSource = hourlyForecastLocalDataSource,
            dailyForecastLocalDataSource = dailyForecastLocalDataSource,
            settingsRepository = settingsRepository,
        )
    }

    private fun sampleCurrentWeatherLocal() = CurrentWeatherLocalModel(
        cityId = "101020100",
        observationTime = "2026-04-08T13:45+08:00",
        temperature = "26",
        feelsLike = "28",
        conditionText = "Sunny",
        conditionIcon = "100",
        humidity = "65",
        windDirection = "East",
        windScale = "3",
        windSpeed = "15",
        precipitation = "0.0",
        pressure = "1012",
        visibility = "16",
        fetchedAtEpochMillis = 100L,
        language = "en",
        unitSystem = "metric",
    )

    private fun sampleHourlyForecastLocal() = HourlyForecastLocalModel(
        cityId = "101020100",
        forecastTime = "2026-04-08T16:00+08:00",
        temperature = "24",
        conditionText = "Cloudy",
        conditionIcon = "101",
        precipitationProbability = "20",
        precipitation = "0.0",
        windDirection = "South",
        windScale = "2",
        windSpeed = "13",
        fetchedAtEpochMillis = 100L,
        language = "en",
        unitSystem = "metric",
    )

    private fun sampleDailyForecastLocal() = DailyForecastLocalModel(
        cityId = "101020100",
        forecastDate = "2026-04-09",
        tempMax = "30",
        tempMin = "22",
        conditionTextDay = "Sunny",
        conditionIconDay = "100",
        precipitationProbability = "10",
        precipitation = "0.0",
        windDirectionDay = "South",
        windScaleDay = "3",
        windSpeedDay = "16",
        fetchedAtEpochMillis = 100L,
        language = "en",
        unitSystem = "metric",
    )

    private fun httpException(statusCode: Int): HttpException {
        val errorResponse = Response.error<Any>(
            statusCode,
            "{}".toResponseBody("application/json".toMediaType()),
        )
        return HttpException(errorResponse)
    }

    private class FakeCurrentWeatherLocalDataSource(
        currentWeather: CurrentWeatherLocalModel? = null,
    ) : CurrentWeatherLocalDataSource {
        private val currentWeatherFlow = MutableStateFlow(currentWeather)
        var upsertedWeather: CurrentWeatherLocalModel? = null
        var lastObservedLanguage: String? = null
        var lastObservedUnitSystem: String? = null

        override fun observeCurrentWeather(
            cityId: String,
            language: String,
            unitSystem: String,
        ): Flow<CurrentWeatherLocalModel?> {
            lastObservedLanguage = language
            lastObservedUnitSystem = unitSystem
            return currentWeatherFlow
        }

        override suspend fun getCurrentWeather(
            cityId: String,
            language: String,
            unitSystem: String,
        ): CurrentWeatherLocalModel? = currentWeatherFlow.value

        override suspend fun upsertCurrentWeather(currentWeather: CurrentWeatherLocalModel) {
            upsertedWeather = currentWeather
            currentWeatherFlow.value = currentWeather
        }

        override suspend fun clearCurrentWeatherCache() {
            currentWeatherFlow.value = null
        }
    }

    private class FakeHourlyForecastLocalDataSource(
        hourlyForecast: List<HourlyForecastLocalModel> = emptyList(),
    ) : HourlyForecastLocalDataSource {
        private val hourlyForecastFlow = MutableStateFlow(hourlyForecast)
        var replacedHourlyForecast: List<HourlyForecastLocalModel>? = null
        var lastObservedLanguage: String? = null
        var lastObservedUnitSystem: String? = null
        var lastReplaceLanguage: String? = null
        var lastReplaceUnitSystem: String? = null

        override fun observeHourlyForecast(
            cityId: String,
            language: String,
            unitSystem: String,
        ): Flow<List<HourlyForecastLocalModel>> {
            lastObservedLanguage = language
            lastObservedUnitSystem = unitSystem
            return hourlyForecastFlow
        }

        override suspend fun getHourlyForecast(
            cityId: String,
            language: String,
            unitSystem: String,
        ): List<HourlyForecastLocalModel> = hourlyForecastFlow.value

        override suspend fun replaceHourlyForecast(
            cityId: String,
            language: String,
            unitSystem: String,
            hourlyForecast: List<HourlyForecastLocalModel>,
        ) {
            lastReplaceLanguage = language
            lastReplaceUnitSystem = unitSystem
            replacedHourlyForecast = hourlyForecast
            hourlyForecastFlow.value = hourlyForecast
        }

        override suspend fun clearHourlyForecastCache() {
            hourlyForecastFlow.value = emptyList()
        }
    }

    private class FakeDailyForecastLocalDataSource(
        dailyForecast: List<DailyForecastLocalModel> = emptyList(),
    ) : DailyForecastLocalDataSource {
        private val dailyForecastFlow = MutableStateFlow(dailyForecast)
        var replacedDailyForecast: List<DailyForecastLocalModel>? = null
        var lastObservedLanguage: String? = null
        var lastObservedUnitSystem: String? = null
        var lastReplaceLanguage: String? = null
        var lastReplaceUnitSystem: String? = null

        override fun observeDailyForecast(
            cityId: String,
            language: String,
            unitSystem: String,
        ): Flow<List<DailyForecastLocalModel>> {
            lastObservedLanguage = language
            lastObservedUnitSystem = unitSystem
            return dailyForecastFlow
        }

        override suspend fun getDailyForecast(
            cityId: String,
            language: String,
            unitSystem: String,
        ): List<DailyForecastLocalModel> = dailyForecastFlow.value

        override suspend fun replaceDailyForecast(
            cityId: String,
            language: String,
            unitSystem: String,
            dailyForecast: List<DailyForecastLocalModel>,
        ) {
            lastReplaceLanguage = language
            lastReplaceUnitSystem = unitSystem
            replacedDailyForecast = dailyForecast
            dailyForecastFlow.value = dailyForecast
        }

        override suspend fun clearDailyForecastCache() {
            dailyForecastFlow.value = emptyList()
        }
    }

    private class FakeSettingsRepository(
        settings: AppSettings = AppSettings(),
    ) : SettingsRepository {
        private val settingsFlow = MutableStateFlow(settings)

        override fun observeAppSettings() = settingsFlow

        override suspend fun getCurrentSettings(): AppSettings = settingsFlow.value

        override suspend fun updateLanguage(language: AppLanguage) = Unit

        override suspend fun updateUnitSystem(unitSystem: UnitSystem) = Unit

        override suspend fun clearWeatherCache() = Unit
    }
}
