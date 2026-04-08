package io.github.yeweijiehust.weatherforecast.feature.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SearchLanguageProvider
import io.github.yeweijiehust.weatherforecast.domain.usecase.SearchCitiesUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CitySearchViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdle() {
        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.resultState).isEqualTo(CitySearchResultState.Idle)
    }

    @Test
    fun search_emitsSearchingThenResults() = runTest {
        val gate = CompletableDeferred<Unit>()
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
        val viewModel = createViewModel(
            repository = FakeCityRepository(
                onSearch = { _, _ ->
                    gate.await()
                    expectedCities
                },
            ),
        )
        viewModel.onQueryChanged("Shanghai")

        viewModel.uiState.test {
            assertThat(awaitItem().resultState).isEqualTo(CitySearchResultState.Idle)

            viewModel.search()
            testDispatcher.scheduler.runCurrent()
            assertThat(awaitItem().resultState).isEqualTo(CitySearchResultState.Searching)

            gate.complete(Unit)
            testDispatcher.scheduler.runCurrent()
            assertThat(awaitItem().resultState).isEqualTo(
                CitySearchResultState.Results(expectedCities),
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun search_emitsEmptyResultWhenRepositoryReturnsNoCities() = runTest {
        val viewModel = createViewModel(
            repository = FakeCityRepository(onSearch = { _, _ -> emptyList() }),
        )
        viewModel.onQueryChanged("Atlantis")

        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.resultState).isEqualTo(
            CitySearchResultState.EmptyResult(query = "Atlantis"),
        )
    }

    @Test
    fun search_emitsErrorWhenRepositoryFails() = runTest {
        val viewModel = createViewModel(
            repository = FakeCityRepository(
                onSearch = { _, _ -> throw IllegalStateException("boom") },
            ),
        )
        viewModel.onQueryChanged("Nanjing")

        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.resultState)
            .isEqualTo(
                CitySearchResultState.Error(
                    query = "Nanjing",
                    message = "We couldn't search right now. Check the connection and try again.",
                ),
            )
    }

    @Test
    fun onQueryChanged_resetsToIdleWhenQueryBecomesBlank() = runTest {
        val viewModel = createViewModel()
        viewModel.onQueryChanged("Guangzhou")
        viewModel.onQueryChanged("   ")

        assertThat(viewModel.uiState.value.query).isEqualTo("   ")
        assertThat(viewModel.uiState.value.resultState).isEqualTo(CitySearchResultState.Idle)
    }

    private fun createViewModel(
        repository: CityRepository = FakeCityRepository(),
    ): CitySearchViewModel {
        return CitySearchViewModel(
            searchCitiesUseCase = SearchCitiesUseCase(
                cityRepository = repository,
                searchLanguageProvider = object : SearchLanguageProvider {
                    override fun currentLanguage(): String = "en"
                },
            ),
        )
    }

    private class FakeCityRepository(
        private val onSearch: suspend (String, String) -> List<City> = { _, _ -> emptyList() },
    ) : CityRepository {
        override suspend fun searchCities(
            query: String,
            language: String,
        ): List<City> = onSearch(query, language)
    }
}
