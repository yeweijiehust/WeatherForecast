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
import io.mockk.coVerify
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
class CitySearchManagementViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun savedCities_areExposedInUiState() = runTest {
        val savedCitiesFlow = MutableStateFlow(
            listOf(
                savedCity(id = "101010100", name = "Beijing", isDefault = true),
            ),
        )
        val viewModel = createViewModel(savedCitiesFlow = savedCitiesFlow)

        dispatcher.scheduler.runCurrent()

        assertThat(viewModel.uiState.value.savedCities).hasSize(1)
        assertThat(viewModel.uiState.value.savedCities.single().isDefault).isTrue()
    }

    @Test
    fun saveCity_emitsDuplicateMessageWhenCityAlreadyExists() = runTest {
        val saveCityUseCase = mockk<SaveCityUseCase>()
        coEvery { saveCityUseCase.invoke(any()) } returns SaveCityResult.Duplicate
        val city = savedCity(id = "101010100", name = "Beijing")
        val viewModel = createViewModel(saveCityUseCase = saveCityUseCase)

        viewModel.events.test {
            viewModel.saveCity(city)
            dispatcher.scheduler.advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(
                CitySearchEvent.ShowMessage(
                    UiText.StringResource(R.string.snackbar_city_already_saved),
                ),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveCity_emitsSavedMessageWhenInsertSucceeds() = runTest {
        val saveCityUseCase = mockk<SaveCityUseCase>()
        coEvery { saveCityUseCase.invoke(any()) } returns SaveCityResult.Saved
        val city = savedCity(id = "101020100", name = "Shanghai")
        val viewModel = createViewModel(saveCityUseCase = saveCityUseCase)

        viewModel.events.test {
            viewModel.saveCity(city)
            dispatcher.scheduler.advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(
                CitySearchEvent.ShowMessage(UiText.StringResource(R.string.snackbar_city_saved)),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setDefaultCity_callsUseCaseAndEmitsMessage() = runTest {
        val setDefaultCityUseCase = mockk<SetDefaultCityUseCase>(relaxed = true)
        val viewModel = createViewModel(setDefaultCityUseCase = setDefaultCityUseCase)

        viewModel.events.test {
            viewModel.setDefaultCity("101020100")
            dispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { setDefaultCityUseCase.invoke("101020100") }
            assertThat(awaitItem()).isEqualTo(
                CitySearchEvent.ShowMessage(
                    UiText.StringResource(R.string.snackbar_default_city_updated),
                ),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removeCity_callsUseCaseAndEmitsMessage() = runTest {
        val removeSavedCityUseCase = mockk<RemoveSavedCityUseCase>(relaxed = true)
        val viewModel = createViewModel(removeSavedCityUseCase = removeSavedCityUseCase)

        viewModel.events.test {
            viewModel.removeCity("101020100")
            dispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 1) { removeSavedCityUseCase.invoke("101020100") }
            assertThat(awaitItem()).isEqualTo(
                CitySearchEvent.ShowMessage(UiText.StringResource(R.string.snackbar_city_removed)),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setDefaultCity_whenUseCaseFails_emitsFailureMessage() = runTest {
        val setDefaultCityUseCase = mockk<SetDefaultCityUseCase>()
        coEvery { setDefaultCityUseCase.invoke("101020100") } throws IllegalStateException("boom")
        val viewModel = createViewModel(setDefaultCityUseCase = setDefaultCityUseCase)

        viewModel.events.test {
            viewModel.setDefaultCity("101020100")
            dispatcher.scheduler.advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(
                CitySearchEvent.ShowMessage(
                    UiText.StringResource(R.string.snackbar_operation_failed_try_again),
                ),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(
        savedCitiesFlow: MutableStateFlow<List<City>> = MutableStateFlow(emptyList()),
        saveCityUseCase: SaveCityUseCase = mockk<SaveCityUseCase>().also {
            coEvery { it.invoke(any()) } returns SaveCityResult.Saved
        },
        setDefaultCityUseCase: SetDefaultCityUseCase = mockk(relaxed = true),
        removeSavedCityUseCase: RemoveSavedCityUseCase = mockk(relaxed = true),
    ): CitySearchViewModel {
        val searchCitiesUseCase = mockk<SearchCitiesUseCase>().also {
            coEvery { it.invoke(any()) } returns emptyList()
        }
        val observeSavedCitiesUseCase = mockk<ObserveSavedCitiesUseCase>().also {
            every { it.invoke() } returns savedCitiesFlow
        }
        val getTopCitySuggestionsUseCase = mockk<GetTopCitySuggestionsUseCase>().also {
            coEvery { it.invoke() } returns emptyList()
        }
        return CitySearchViewModel(
            searchCitiesUseCase = searchCitiesUseCase,
            getTopCitySuggestionsUseCase = getTopCitySuggestionsUseCase,
            observeSavedCitiesUseCase = observeSavedCitiesUseCase,
            saveCityUseCase = saveCityUseCase,
            setDefaultCityUseCase = setDefaultCityUseCase,
            removeSavedCityUseCase = removeSavedCityUseCase,
        )
    }

    private fun savedCity(
        id: String,
        name: String,
        isDefault: Boolean = false,
    ): City {
        return City(
            id = id,
            name = name,
            adm1 = name,
            adm2 = name,
            country = "China",
            lat = "0",
            lon = "0",
            timeZone = "Asia/Shanghai",
            isDefault = isDefault,
        )
    }
}
