package io.github.yeweijiehust.weatherforecast.feature.home

import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveCurrentWeatherUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveDefaultCityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshCurrentWeatherUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
        val viewModel = createViewModel(defaultCity = null)

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state).isEqualTo(HomeState.EmptyNoCity)
    }

    @Test
    fun state_exposesCurrentWeatherContentWhenCacheExists() = runTest {
        val defaultCity = sampleCity()
        val cachedWeather = sampleCurrentWeather(cityId = defaultCity.id)
        val viewModel = createViewModel(
            defaultCity = defaultCity,
            currentWeather = cachedWeather,
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state).isEqualTo(
            HomeState.Content(
                city = defaultCity,
                currentWeather = cachedWeather,
            ),
        )
    }

    @Test
    fun state_becomesErrorWhenRefreshFailsWithoutCache() = runTest {
        val defaultCity = sampleCity()
        val viewModel = createViewModel(
            defaultCity = defaultCity,
            currentWeather = null,
            refreshFailure = IllegalStateException("boom"),
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state).isEqualTo(HomeState.ErrorNoCache(defaultCity))
    }

    private fun createViewModel(
        defaultCity: City?,
        currentWeather: CurrentWeather? = null,
        refreshFailure: Throwable? = null,
    ): HomeViewModel {
        val weatherRepository = FakeWeatherRepository(
            currentWeather = currentWeather,
            refreshFailure = refreshFailure,
        )
        return HomeViewModel(
            observeDefaultCityUseCase = ObserveDefaultCityUseCase(
                cityRepository = FakeCityRepository(defaultCity = defaultCity),
            ),
            observeCurrentWeatherUseCase = ObserveCurrentWeatherUseCase(weatherRepository),
            refreshCurrentWeatherUseCase = RefreshCurrentWeatherUseCase(weatherRepository),
        )
    }

    private fun sampleCity(): City {
        return City(
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
    }

    private fun sampleCurrentWeather(cityId: String): CurrentWeather {
        return CurrentWeather(
            cityId = cityId,
            observationTime = "2026-04-08T13:45+08:00",
            temperature = "26",
            feelsLike = "28",
            conditionText = "Sunny",
            conditionIcon = "100",
            humidity = "65",
            windDirection = "East",
            windScale = "3",
            windSpeed = "15",
            precipitation = "0.0",
            pressure = "1012",
            visibility = "16",
            fetchedAtEpochMillis = 100L,
        )
    }

    private class FakeCityRepository(
        defaultCity: City?,
    ) : CityRepository {
        private val defaultCityFlow = MutableStateFlow(defaultCity)

        override suspend fun searchCities(query: String, language: String): List<City> = emptyList()

        override fun observeSavedCities(): Flow<List<City>> = MutableStateFlow(emptyList())

        override fun observeDefaultCity(): Flow<City?> = defaultCityFlow

        override suspend fun saveCity(city: City) = error("Not used in HomeViewModelTest")

        override suspend fun setDefaultCity(cityId: String) = Unit

        override suspend fun removeCity(cityId: String) = Unit
    }

    private class FakeWeatherRepository(
        currentWeather: CurrentWeather?,
        private val refreshFailure: Throwable?,
    ) : WeatherRepository {
        private val currentWeatherFlow = MutableStateFlow(currentWeather)

        override fun observeCurrentWeather(cityId: String): Flow<CurrentWeather?> = currentWeatherFlow

        override suspend fun refreshCurrentWeather(cityId: String) {
            refreshFailure?.let { throw it }
        }
    }
}
