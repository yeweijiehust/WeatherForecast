package io.github.yeweijiehust.weatherforecast.data.repository

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.data.local.source.AppSettingsPreferencesDataSource
import io.github.yeweijiehust.weatherforecast.data.local.source.WeatherCacheCleaner
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultSettingsRepositoryTest {
    @Test
    fun observeAndGetCurrentSettings_delegateToDataSource() = runTest {
        val dataSource = FakeAppSettingsPreferencesDataSource(
            initialSettings = AppSettings(
                language = AppLanguage.SimplifiedChinese,
                unitSystem = UnitSystem.Imperial,
            ),
        )
        val repository = DefaultSettingsRepository(
            appSettingsPreferencesDataSource = dataSource,
            weatherCacheCleaner = mockk(relaxed = true),
        )

        assertThat(repository.observeAppSettings().first()).isEqualTo(dataSource.getCurrentSettings())
        assertThat(repository.getCurrentSettings()).isEqualTo(dataSource.getCurrentSettings())
    }

    @Test
    fun updateLanguageAndUnitSystem_persistValues() = runTest {
        val dataSource = FakeAppSettingsPreferencesDataSource()
        val repository = DefaultSettingsRepository(
            appSettingsPreferencesDataSource = dataSource,
            weatherCacheCleaner = mockk(relaxed = true),
        )

        repository.updateLanguage(AppLanguage.SimplifiedChinese)
        repository.updateUnitSystem(UnitSystem.Imperial)

        assertThat(dataSource.getCurrentSettings()).isEqualTo(
            AppSettings(
                language = AppLanguage.SimplifiedChinese,
                unitSystem = UnitSystem.Imperial,
            ),
        )
    }

    @Test
    fun clearWeatherCache_callsCleaner() = runTest {
        val weatherCacheCleaner = mockk<WeatherCacheCleaner>(relaxed = true)
        val repository = DefaultSettingsRepository(
            appSettingsPreferencesDataSource = FakeAppSettingsPreferencesDataSource(),
            weatherCacheCleaner = weatherCacheCleaner,
        )

        repository.clearWeatherCache()

        coVerify(exactly = 1) { weatherCacheCleaner.clearWeatherCache() }
    }

    private class FakeAppSettingsPreferencesDataSource(
        initialSettings: AppSettings = AppSettings(),
    ) : AppSettingsPreferencesDataSource {
        private val settingsFlow = MutableStateFlow(initialSettings)

        override fun observeAppSettings(): Flow<AppSettings> = settingsFlow

        override suspend fun getCurrentSettings(): AppSettings = settingsFlow.value

        override suspend fun updateLanguage(language: AppLanguage) {
            settingsFlow.value = settingsFlow.value.copy(language = language)
        }

        override suspend fun updateUnitSystem(unitSystem: UnitSystem) {
            settingsFlow.value = settingsFlow.value.copy(unitSystem = unitSystem)
        }
    }
}
