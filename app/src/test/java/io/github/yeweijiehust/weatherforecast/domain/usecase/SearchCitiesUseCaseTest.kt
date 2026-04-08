package io.github.yeweijiehust.weatherforecast.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SearchLanguageProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchCitiesUseCaseTest {
    @Test
    fun invoke_returnsEmptyWithoutCallingRepository_whenQueryIsBlank() = runTest {
        val repository = mockk<CityRepository>()
        val searchLanguageProvider = mockk<SearchLanguageProvider>()
        val useCase = SearchCitiesUseCase(
            cityRepository = repository,
            searchLanguageProvider = searchLanguageProvider,
        )

        val result = useCase("   ")

        assertThat(result).isEmpty()
        coVerify(exactly = 0) { repository.searchCities(any(), any()) }
    }

    @Test
    fun invoke_trimsQueryAndUsesCurrentLanguage() = runTest {
        val expectedCities = listOf(
            City(
                id = "101010100",
                name = "Beijing",
                adm1 = "Beijing",
                adm2 = "Beijing",
                country = "China",
                lat = "39.90",
                lon = "116.40",
                timeZone = "Asia/Shanghai",
            ),
        )
        val repository = mockk<CityRepository>()
        val searchLanguageProvider = mockk<SearchLanguageProvider>()
        every { searchLanguageProvider.currentLanguage() } returns "zh"
        coEvery {
            repository.searchCities(
                query = "Beijing",
                language = "zh",
            )
        } returns expectedCities
        val useCase = SearchCitiesUseCase(
            cityRepository = repository,
            searchLanguageProvider = searchLanguageProvider,
        )

        val result = useCase("  Beijing  ")

        assertThat(result).isEqualTo(expectedCities)
        coVerify(exactly = 1) {
            repository.searchCities(
                query = "Beijing",
                language = "zh",
            )
        }
    }
}
