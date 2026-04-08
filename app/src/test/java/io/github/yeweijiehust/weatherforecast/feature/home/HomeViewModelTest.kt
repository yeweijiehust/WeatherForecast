package io.github.yeweijiehust.weatherforecast.feature.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.R
import io.github.yeweijiehust.weatherforecast.core.ui.UiText
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveCurrentWeatherUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveDailyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveDefaultCityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveHourlyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshCurrentWeatherUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshDailyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshHourlyForecastUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
    fun state_startsUninitialized_thenBecomesEmptyWhenNoDefaultCityExists() = runTest {
        val viewModel = createViewModel(defaultCity = null)

        assertThat(viewModel.uiState.value.state).isEqualTo(HomeState.Uninitialized)
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state).isEqualTo(HomeState.EmptyNoCity)
    }

    @Test
    fun state_flowsLoadingToContentWhenCacheExists() = runTest {
        val defaultCity = sampleCity()
        val repository = FakeWeatherRepository(
            currentWeather = sampleCurrentWeather(defaultCity.id),
            hourlyForecast = listOf(sampleHourlyForecast(defaultCity.id)),
            dailyForecast = listOf(sampleDailyForecast(defaultCity.id)),
        )
        val viewModel = createViewModel(
            defaultCity = defaultCity,
            weatherRepository = repository,
        )

        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value.state
        assertThat(state).isInstanceOf(HomeState.Content::class.java)
        val snapshot = (state as HomeState.Content).snapshot
        assertThat(snapshot.city.id).isEqualTo(defaultCity.id)
        assertThat(snapshot.currentWeather.conditionText).isEqualTo("Sunny")
        assertThat(snapshot.hourlyForecast).hasSize(1)
        assertThat(snapshot.dailyForecast).hasSize(1)
    }

    @Test
    fun state_transitionsContentToRefreshingToContentOnManualRefresh() = runTest {
        val defaultCity = sampleCity()
        val repository = FakeWeatherRepository(
            currentWeather = sampleCurrentWeather(defaultCity.id),
            hourlyForecast = listOf(sampleHourlyForecast(defaultCity.id)),
            dailyForecast = listOf(sampleDailyForecast(defaultCity.id)),
        )
        val viewModel = createViewModel(
            defaultCity = defaultCity,
            weatherRepository = repository,
        )
        dispatcher.scheduler.advanceUntilIdle()

        repository.refreshDelayMillis = 1_000L
        viewModel.onPullToRefresh()
        dispatcher.scheduler.runCurrent()

        assertThat(viewModel.uiState.value.state).isInstanceOf(HomeState.Refreshing::class.java)
        dispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.state).isInstanceOf(HomeState.Content::class.java)
    }

    @Test
    fun state_becomesContentWithStaleCacheWhenRefreshFailsWithCache() = runTest {
        val defaultCity = sampleCity()
        val repository = FakeWeatherRepository(
            currentWeather = sampleCurrentWeather(defaultCity.id),
            hourlyForecast = listOf(sampleHourlyForecast(defaultCity.id)),
            dailyForecast = listOf(sampleDailyForecast(defaultCity.id)),
        )
        val viewModel = createViewModel(
            defaultCity = defaultCity,
            weatherRepository = repository,
        )
        dispatcher.scheduler.advanceUntilIdle()

        repository.failNextCurrentRefresh = true
        viewModel.onPullToRefresh()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value.state
        assertThat(state).isInstanceOf(HomeState.ContentWithStaleCache::class.java)
        assertThat((state as HomeState.ContentWithStaleCache).snapshot.city.id).isEqualTo(defaultCity.id)
    }

    @Test
    fun event_emitsStaleCacheSnackbarWhenRefreshFailsWithCache() = runTest {
        val defaultCity = sampleCity()
        val repository = FakeWeatherRepository(
            currentWeather = sampleCurrentWeather(defaultCity.id),
            hourlyForecast = listOf(sampleHourlyForecast(defaultCity.id)),
            dailyForecast = listOf(sampleDailyForecast(defaultCity.id)),
        )
        val viewModel = createViewModel(
            defaultCity = defaultCity,
            weatherRepository = repository,
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.events.test {
            repository.failNextCurrentRefresh = true
            viewModel.onPullToRefresh()
            dispatcher.scheduler.advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(
                HomeEvent.ShowMessage(
                    message = UiText.StringResource(R.string.snackbar_stale_cache_shown),
                    action = HomeEventAction.RetryRefresh,
                ),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun state_becomesContentWithStaleCacheWhenHourlyRefreshFailsWithHourlyCache() = runTest {
        val defaultCity = sampleCity()
        val repository = FakeWeatherRepository(
            currentWeather = sampleCurrentWeather(defaultCity.id),
            hourlyForecast = listOf(sampleHourlyForecast(defaultCity.id)),
            dailyForecast = listOf(sampleDailyForecast(defaultCity.id)),
        )
        val viewModel = createViewModel(
            defaultCity = defaultCity,
            weatherRepository = repository,
        )
        dispatcher.scheduler.advanceUntilIdle()

        repository.failNextHourlyRefresh = true
        viewModel.onPullToRefresh()
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state)
            .isInstanceOf(HomeState.ContentWithStaleCache::class.java)
    }

    @Test
    fun state_staysContentWhenHourlyRefreshFailsWithoutHourlyCache() = runTest {
        val defaultCity = sampleCity()
        val repository = FakeWeatherRepository(
            currentWeather = sampleCurrentWeather(defaultCity.id),
            hourlyForecast = emptyList(),
            dailyForecast = listOf(sampleDailyForecast(defaultCity.id)),
        )
        val viewModel = createViewModel(
            defaultCity = defaultCity,
            weatherRepository = repository,
        )
        dispatcher.scheduler.advanceUntilIdle()

        repository.failNextHourlyRefresh = true
        viewModel.onPullToRefresh()
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state).isInstanceOf(HomeState.Content::class.java)
    }

    @Test
    fun event_emitsRefreshFailedSnackbarWhenHourlyRefreshFailsWithoutHourlyCache() = runTest {
        val defaultCity = sampleCity()
        val repository = FakeWeatherRepository(
            currentWeather = sampleCurrentWeather(defaultCity.id),
            hourlyForecast = emptyList(),
            dailyForecast = listOf(sampleDailyForecast(defaultCity.id)),
        )
        val viewModel = createViewModel(
            defaultCity = defaultCity,
            weatherRepository = repository,
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.events.test {
            repository.failNextHourlyRefresh = true
            viewModel.onPullToRefresh()
            dispatcher.scheduler.advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(
                HomeEvent.ShowMessage(
                    message = UiText.StringResource(R.string.snackbar_refresh_failed),
                    action = HomeEventAction.RetryRefresh,
                ),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun state_becomesErrorWhenRefreshFailsWithoutCache() = runTest {
        val defaultCity = sampleCity()
        val repository = FakeWeatherRepository(
            currentWeather = null,
            hourlyForecast = emptyList(),
            dailyForecast = emptyList(),
            failNextCurrentRefresh = true,
        )
        val viewModel = createViewModel(
            defaultCity = defaultCity,
            weatherRepository = repository,
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state).isEqualTo(HomeState.ErrorNoCache(defaultCity))
    }

    @Test
    fun event_emitsRefreshFailedSnackbarWhenRefreshFailsWithoutCache() = runTest {
        val defaultCity = sampleCity()
        val repository = FakeWeatherRepository(
            currentWeather = null,
            hourlyForecast = emptyList(),
            dailyForecast = emptyList(),
        )
        val viewModel = createViewModel(
            defaultCity = defaultCity,
            weatherRepository = repository,
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.events.test {
            repository.failNextCurrentRefresh = true
            viewModel.onPullToRefresh()
            dispatcher.scheduler.advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(
                HomeEvent.ShowMessage(
                    message = UiText.StringResource(R.string.snackbar_refresh_failed),
                    action = HomeEventAction.RetryRefresh,
                ),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(
        defaultCity: City?,
        weatherRepository: FakeWeatherRepository = FakeWeatherRepository(),
    ): HomeViewModel {
        return HomeViewModel(
            observeDefaultCityUseCase = ObserveDefaultCityUseCase(
                cityRepository = FakeCityRepository(defaultCity = defaultCity),
            ),
            observeCurrentWeatherUseCase = ObserveCurrentWeatherUseCase(weatherRepository),
            observeHourlyForecastUseCase = ObserveHourlyForecastUseCase(weatherRepository),
            observeDailyForecastUseCase = ObserveDailyForecastUseCase(weatherRepository),
            refreshCurrentWeatherUseCase = RefreshCurrentWeatherUseCase(weatherRepository),
            refreshHourlyForecastUseCase = RefreshHourlyForecastUseCase(weatherRepository),
            refreshDailyForecastUseCase = RefreshDailyForecastUseCase(weatherRepository),
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

    private fun sampleHourlyForecast(cityId: String): HourlyForecast {
        return HourlyForecast(
            cityId = cityId,
            forecastTime = "2026-04-08T16:00+08:00",
            temperature = "24",
            conditionText = "Cloudy",
            conditionIcon = "101",
            precipitationProbability = "20",
            precipitation = "0.0",
            windDirection = "South",
            windScale = "2",
            windSpeed = "13",
            fetchedAtEpochMillis = 100L,
        )
    }

    private fun sampleDailyForecast(cityId: String): DailyForecast {
        return DailyForecast(
            cityId = cityId,
            forecastDate = "2026-04-09",
            tempMax = "30",
            tempMin = "22",
            conditionTextDay = "Sunny",
            conditionIconDay = "100",
            precipitationProbability = "10",
            precipitation = "0.0",
            windDirectionDay = "South",
            windScaleDay = "3",
            windSpeedDay = "16",
            fetchedAtEpochMillis = 100L,
        )
    }

    private class FakeCityRepository(
        defaultCity: City?,
    ) : CityRepository {
        private val defaultCityFlow = MutableStateFlow(defaultCity)

        override suspend fun searchCities(query: String, language: String): List<City> = emptyList()

        override suspend fun fetchTopCities(language: String, number: Int): List<City> = emptyList()

        override fun observeSavedCities(): Flow<List<City>> = MutableStateFlow(emptyList())

        override fun observeDefaultCity(): Flow<City?> = defaultCityFlow

        override suspend fun saveCity(city: City) = error("Not used in HomeViewModelTest")

        override suspend fun setDefaultCity(cityId: String) = Unit

        override suspend fun removeCity(cityId: String) = Unit
    }

    private class FakeWeatherRepository(
        currentWeather: CurrentWeather? = null,
        hourlyForecast: List<HourlyForecast> = emptyList(),
        dailyForecast: List<DailyForecast> = emptyList(),
        var failNextCurrentRefresh: Boolean = false,
        var failNextHourlyRefresh: Boolean = false,
        var failNextDailyRefresh: Boolean = false,
        var refreshDelayMillis: Long = 0L,
    ) : WeatherRepository {
        private val currentWeatherFlow = MutableStateFlow(currentWeather)
        private val hourlyForecastFlow = MutableStateFlow(hourlyForecast)
        private val dailyForecastFlow = MutableStateFlow(dailyForecast)

        override fun observeCurrentWeather(cityId: String): Flow<CurrentWeather?> = currentWeatherFlow

        override suspend fun refreshCurrentWeather(cityId: String) {
            if (refreshDelayMillis > 0) {
                delay(refreshDelayMillis)
            }
            if (failNextCurrentRefresh) {
                failNextCurrentRefresh = false
                throw IllegalStateException("boom")
            }
        }

        override fun observeHourlyForecast(cityId: String): Flow<List<HourlyForecast>> = hourlyForecastFlow

        override suspend fun refreshHourlyForecast(cityId: String) {
            if (failNextHourlyRefresh) {
                failNextHourlyRefresh = false
                throw IllegalStateException("hourly-boom")
            }
        }

        override fun observeDailyForecast(cityId: String): Flow<List<DailyForecast>> = dailyForecastFlow

        override suspend fun refreshDailyForecast(cityId: String) {
            if (failNextDailyRefresh) {
                failNextDailyRefresh = false
                throw IllegalStateException("daily-boom")
            }
        }

        override suspend fun fetchWeatherAlerts(
            latitude: String,
            longitude: String,
        ): WeatherAlertFetchResult = WeatherAlertFetchResult.Empty
    }
}
