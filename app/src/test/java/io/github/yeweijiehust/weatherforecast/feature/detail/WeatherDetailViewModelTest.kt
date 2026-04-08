package io.github.yeweijiehust.weatherforecast.feature.detail

import com.google.common.truth.Truth.assertThat
import androidx.lifecycle.SavedStateHandle
import io.github.yeweijiehust.weatherforecast.core.navigation.WeatherForecastDestination
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveSavedCitiesUseCase
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
class WeatherDetailViewModelTest {
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
    fun init_withMatchingCityId_emitsContent() = runTest {
        val citiesFlow = MutableStateFlow(listOf(sampleCity(id = "101020100")))
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf<String, Any?>(
                    WeatherForecastDestination.CITY_ID_ARG to "101020100",
                ),
            ),
            citiesFlow = citiesFlow,
        )

        dispatcher.scheduler.runCurrent()

        assertThat(viewModel.uiState.value.state).isInstanceOf(WeatherDetailState.Content::class.java)
        assertThat((viewModel.uiState.value.state as WeatherDetailState.Content).city.id)
            .isEqualTo("101020100")
    }

    @Test
    fun init_withUnknownCityId_emitsErrorNoData() = runTest {
        val citiesFlow = MutableStateFlow(listOf(sampleCity(id = "101020101")))
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf<String, Any?>(
                    WeatherForecastDestination.CITY_ID_ARG to "101020100",
                ),
            ),
            citiesFlow = citiesFlow,
        )

        dispatcher.scheduler.runCurrent()

        assertThat(viewModel.uiState.value.state).isEqualTo(
            WeatherDetailState.ErrorNoData(cityId = "101020100"),
        )
    }

    @Test
    fun init_withoutCityId_emitsErrorNoData() = runTest {
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(),
            citiesFlow = MutableStateFlow(emptyList()),
        )

        dispatcher.scheduler.runCurrent()

        assertThat(viewModel.uiState.value.state).isEqualTo(
            WeatherDetailState.ErrorNoData(cityId = ""),
        )
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle,
        citiesFlow: MutableStateFlow<List<City>>,
    ): WeatherDetailViewModel {
        val observeSavedCitiesUseCase = mockk<ObserveSavedCitiesUseCase>().also { useCase ->
            every { useCase.invoke() } returns citiesFlow
        }
        return WeatherDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeSavedCitiesUseCase = observeSavedCitiesUseCase,
        )
    }

    private fun sampleCity(id: String): City {
        return City(
            id = id,
            name = "Shanghai",
            adm1 = "Shanghai",
            adm2 = "Shanghai",
            country = "China",
            lat = "31.23",
            lon = "121.47",
            timeZone = "Asia/Shanghai",
            isDefault = true,
        )
    }
}
