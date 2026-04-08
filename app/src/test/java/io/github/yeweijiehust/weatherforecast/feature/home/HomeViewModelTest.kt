package io.github.yeweijiehust.weatherforecast.feature.home

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.SaveCityResult
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveDefaultCityUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
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
    fun state_isEmptyWhenNoDefaultCityExists() = runTest {
        val viewModel = HomeViewModel(
            observeDefaultCityUseCase = ObserveDefaultCityUseCase(
                cityRepository = FakeCityRepository(defaultCity = null),
            ),
        )
        dispatcher.scheduler.runCurrent()

        assertThat(viewModel.uiState.value.defaultCity).isNull()
    }

    @Test
    fun state_exposesDefaultCityFromRepository() = runTest {
        val defaultCity = City(
            id = "101020100",
            name = "Shanghai",
            adm1 = "Shanghai",
            adm2 = "Shanghai",
            country = "China",
            lat = "31.23",
            lon = "121.47",
            timeZone = "Asia/Shanghai",
            isDefault = true,
        )
        val viewModel = HomeViewModel(
            observeDefaultCityUseCase = ObserveDefaultCityUseCase(
                cityRepository = FakeCityRepository(defaultCity = defaultCity),
            ),
        )
        dispatcher.scheduler.runCurrent()

        assertThat(viewModel.uiState.value.defaultCity?.id).isEqualTo("101020100")
        assertThat(viewModel.uiState.value.defaultCity?.isDefault).isTrue()
    }

    private class FakeCityRepository(
        defaultCity: City?,
    ) : CityRepository {
        private val defaultCityFlow = MutableStateFlow(defaultCity)

        override suspend fun searchCities(query: String, language: String): List<City> = emptyList()

        override fun observeSavedCities(): Flow<List<City>> = MutableStateFlow(emptyList())

        override fun observeDefaultCity(): Flow<City?> = defaultCityFlow

        override suspend fun saveCity(city: City): SaveCityResult = SaveCityResult.Saved

        override suspend fun setDefaultCity(cityId: String) = Unit

        override suspend fun removeCity(cityId: String) = Unit
    }
}
