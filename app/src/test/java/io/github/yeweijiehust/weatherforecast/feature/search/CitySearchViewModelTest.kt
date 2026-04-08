package io.github.yeweijiehust.weatherforecast.feature.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.ui.UiText
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.SaveCityResult
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveSavedCitiesUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RemoveSavedCityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.SaveCityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.SearchCitiesUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.SetDefaultCityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetTopCitySuggestionsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    fun init_loadsTopCitySuggestionsForIdleState() = runTest {
        val suggestions = listOf(
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
        val viewModel = createViewModel(
            getTopCitySuggestionsUseCase = mockk<GetTopCitySuggestionsUseCase>().also { useCase ->
                coEvery { useCase.invoke() } returns suggestions
            },
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.topCitySuggestions).isEqualTo(suggestions)
        assertThat(viewModel.uiState.value.resultState).isEqualTo(CitySearchResultState.Idle)
    }

    @Test
    fun search_emitsSearchingThenResults() = runTest {
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
            searchCitiesUseCase = mockk<SearchCitiesUseCase>().also { useCase ->
                coEvery { useCase.invoke("Shanghai") } returns expectedCities
            },
        )
        viewModel.onQueryChanged("Shanghai")

        viewModel.uiState.test {
            assertThat(awaitItem().resultState).isEqualTo(CitySearchResultState.Idle)

            viewModel.search()
            testDispatcher.scheduler.runCurrent()
            assertThat(awaitItem().resultState).isEqualTo(CitySearchResultState.Searching)

            assertThat(awaitItem().resultState).isEqualTo(
                CitySearchResultState.Results(expectedCities),
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun search_emitsEmptyResultWhenRepositoryReturnsNoCities() = runTest {
        val viewModel = createViewModel(
            searchCitiesUseCase = mockk<SearchCitiesUseCase>().also { useCase ->
                coEvery { useCase.invoke("Atlantis") } returns emptyList()
            },
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
            searchCitiesUseCase = mockk<SearchCitiesUseCase>().also { useCase ->
                coEvery { useCase.invoke("Nanjing") } throws IllegalStateException("boom")
            },
        )
        viewModel.onQueryChanged("Nanjing")

        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.resultState)
            .isEqualTo(
                CitySearchResultState.Error(
                    query = "Nanjing",
                    message = UiText.StringResource(R.string.search_error_generic),
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

    @Test
    fun onTopCitySuggestionSelected_setsQueryAndSearches() = runTest {
        val result = listOf(
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
            searchCitiesUseCase = mockk<SearchCitiesUseCase>().also { useCase ->
                coEvery { useCase.invoke("Shanghai") } returns result
            },
            getTopCitySuggestionsUseCase = mockk<GetTopCitySuggestionsUseCase>().also { useCase ->
                coEvery { useCase.invoke() } returns listOf(result.single())
            },
        )

        viewModel.onTopCitySuggestionSelected("Shanghai")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.query).isEqualTo("Shanghai")
        assertThat(viewModel.uiState.value.resultState).isEqualTo(
            CitySearchResultState.Results(cities = result),
        )
    }

    private fun createViewModel(
        searchCitiesUseCase: SearchCitiesUseCase = mockk<SearchCitiesUseCase>().also { useCase ->
            coEvery { useCase.invoke(any()) } returns emptyList()
        },
        observeSavedCitiesUseCase: ObserveSavedCitiesUseCase = mockk<ObserveSavedCitiesUseCase>().also { useCase ->
            every { useCase.invoke() } returns MutableStateFlow(emptyList())
        },
        getTopCitySuggestionsUseCase: GetTopCitySuggestionsUseCase = mockk<GetTopCitySuggestionsUseCase>().also { useCase ->
            coEvery { useCase.invoke() } returns emptyList()
        },
    ): CitySearchViewModel {
        return CitySearchViewModel(
            searchCitiesUseCase = searchCitiesUseCase,
            getTopCitySuggestionsUseCase = getTopCitySuggestionsUseCase,
            observeSavedCitiesUseCase = observeSavedCitiesUseCase,
            saveCityUseCase = mockk<SaveCityUseCase>().also { useCase ->
                coEvery { useCase.invoke(any()) } returns SaveCityResult.Saved
            },
            setDefaultCityUseCase = mockk<SetDefaultCityUseCase>(relaxed = true),
            removeSavedCityUseCase = mockk<RemoveSavedCityUseCase>(relaxed = true),
        )
    }
}
