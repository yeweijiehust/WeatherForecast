package io.github.yeweijiehust.weatherforecast.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SearchLanguageProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchCitiesUseCaseTest {
    @Test
    fun invoke_returnsEmptyWithoutCallingRepository_whenQueryIsBlank() = runTest {
        val repository = FakeCityRepository()
        val useCase = SearchCitiesUseCase(
            cityRepository = repository,
            searchLanguageProvider = FakeSearchLanguageProvider(language = "zh"),
        )

        val result = useCase("   ")

        assertThat(result).isEmpty()
        assertThat(repository.recordedQuery).isNull()
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
        val repository = FakeCityRepository(result = expectedCities)
        val useCase = SearchCitiesUseCase(
            cityRepository = repository,
            searchLanguageProvider = FakeSearchLanguageProvider(language = "zh"),
        )

        val result = useCase("  Beijing  ")

        assertThat(result).isEqualTo(expectedCities)
        assertThat(repository.recordedQuery).isEqualTo("Beijing")
        assertThat(repository.recordedLanguage).isEqualTo("zh")
    }

    private class FakeCityRepository(
        private val result: List<City> = emptyList(),
    ) : CityRepository {
        var recordedQuery: String? = null
        var recordedLanguage: String? = null

        override suspend fun searchCities(
            query: String,
            language: String,
        ): List<City> {
            recordedQuery = query
            recordedLanguage = language
            return result
        }
    }

    private class FakeSearchLanguageProvider(
        private val language: String,
    ) : SearchLanguageProvider {
        override fun currentLanguage(): String = language
    }
}
