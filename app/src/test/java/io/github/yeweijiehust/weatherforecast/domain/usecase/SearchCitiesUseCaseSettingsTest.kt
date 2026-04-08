package io.github.yeweijiehust.weatherforecast.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchCitiesUseCaseSettingsTest {
    @Test
    fun invoke_usesLanguageFromSettingsRepository() = runTest {
        val expectedCities = listOf(
            City(
                id = "101020100",
                name = "Shanghai",
                adm1 = "Shanghai",
                adm2 = "Shanghai",
                country = "China",
                lat = "31.23",
                lon = "121.47",
                timeZone = "Asia/Shanghai",
            ),
        )
        val cityRepository = mockk<CityRepository>()
        val settingsRepository = mockk<SettingsRepository>()
        coEvery { settingsRepository.getCurrentSettings() } returns AppSettings(
            language = AppLanguage.SimplifiedChinese,
            unitSystem = UnitSystem.Metric,
        )
        coEvery {
            cityRepository.searchCities(
                query = "Shanghai",
                language = "zh",
            )
        } returns expectedCities
        val useCase = SearchCitiesUseCase(
            cityRepository = cityRepository,
            settingsRepository = settingsRepository,
        )

        val result = useCase("Shanghai")

        assertThat(result).isEqualTo(expectedCities)
        coVerify(exactly = 1) { settingsRepository.getCurrentSettings() }
        coVerify(exactly = 1) {
            cityRepository.searchCities(
                query = "Shanghai",
                language = "zh",
            )
        }
    }

    private class UnusedSettingsRepository : SettingsRepository {
        override fun observeAppSettings(): Flow<AppSettings> = emptyFlow()
        override suspend fun getCurrentSettings(): AppSettings = AppSettings()
        override suspend fun updateLanguage(language: AppLanguage) = Unit
        override suspend fun updateUnitSystem(unitSystem: UnitSystem) = Unit
        override suspend fun clearWeatherCache() = Unit
    }
}
