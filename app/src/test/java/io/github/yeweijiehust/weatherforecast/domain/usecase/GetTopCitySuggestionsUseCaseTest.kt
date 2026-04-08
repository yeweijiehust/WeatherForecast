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
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetTopCitySuggestionsUseCaseTest {
    @Test
    fun invoke_usesCurrentLanguageFromSettings() = runTest {
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
        coEvery { cityRepository.fetchTopCities(language = "zh") } returns expectedCities
        val useCase = GetTopCitySuggestionsUseCase(
            cityRepository = cityRepository,
            settingsRepository = settingsRepository,
        )

        val result = useCase()

        assertThat(result).isEqualTo(expectedCities)
        coVerify(exactly = 1) { settingsRepository.getCurrentSettings() }
        coVerify(exactly = 1) { cityRepository.fetchTopCities(language = "zh") }
    }
}
