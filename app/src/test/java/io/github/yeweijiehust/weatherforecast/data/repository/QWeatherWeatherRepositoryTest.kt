package io.github.yeweijiehust.weatherforecast.data.repository

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.local.model.CurrentWeatherLocalModel
import io.github.yeweijiehust.weatherforecast.data.local.source.CurrentWeatherLocalDataSource
import io.github.yeweijiehust.weatherforecast.data.remote.api.WeatherApiService
import io.github.yeweijiehust.weatherforecast.data.remote.config.QWeatherConfig
import io.github.yeweijiehust.weatherforecast.data.remote.dto.CurrentWeatherDto
import io.github.yeweijiehust.weatherforecast.data.remote.dto.CurrentWeatherResponseDto
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class QWeatherWeatherRepositoryTest {
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
    fun refreshCurrentWeather_throwsWhenApiConfigMissing() = runTest {
        val repository = QWeatherWeatherRepository(
            weatherApiService = mockk(),
            qWeatherConfig = QWeatherConfig(apiKey = "", apiHost = ""),
            currentWeatherLocalDataSource = FakeCurrentWeatherLocalDataSource(),
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
        settingsRepository: SettingsRepository = FakeSettingsRepository(),
    ): QWeatherWeatherRepository {
        return QWeatherWeatherRepository(
            weatherApiService = weatherApiService,
            qWeatherConfig = QWeatherConfig(
                apiKey = "test-key",
                apiHost = "example.com",
            ),
            currentWeatherLocalDataSource = currentWeatherLocalDataSource,
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
