package io.github.yeweijiehust.weatherforecast.feature.detail

import com.google.common.truth.Truth.assertThat
import androidx.lifecycle.SavedStateHandle
import io.github.yeweijiehust.weatherforecast.core.navigation.WeatherForecastDestination
import io.github.yeweijiehust.weatherforecast.domain.model.AirQuality
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlert
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetAirQualityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetWeatherAlertsUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveSavedCitiesUseCase
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
            getWeatherAlertsUseCase = mockk<GetWeatherAlertsUseCase>().also { useCase ->
                coEvery { useCase.invoke(latitude = "31.23", longitude = "121.47") } returns
                    WeatherAlertFetchResult.Available(
                        alerts = listOf(sampleAlert()),
                    )
            },
            getAirQualityUseCase = mockk<GetAirQualityUseCase>().also { useCase ->
                coEvery { useCase.invoke(latitude = "31.23", longitude = "121.47") } returns
                    AirQualityFetchResult.Available(airQuality = sampleAirQuality())
            },
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state).isInstanceOf(WeatherDetailState.Content::class.java)
        val contentState = viewModel.uiState.value.state as WeatherDetailState.Content
        assertThat(contentState.city.id)
            .isEqualTo("101020100")
        assertThat(contentState.alerts).hasSize(1)
        assertThat(contentState.airQuality?.aqi).isEqualTo("86")
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
    fun init_withNoAlertData_emitsContentWithEmptyAlerts() = runTest {
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf<String, Any?>(
                    WeatherForecastDestination.CITY_ID_ARG to "101020100",
                ),
            ),
            citiesFlow = MutableStateFlow(listOf(sampleCity(id = "101020100"))),
            getWeatherAlertsUseCase = mockk<GetWeatherAlertsUseCase>().also { useCase ->
                coEvery { useCase.invoke(latitude = "31.23", longitude = "121.47") } returns
                    WeatherAlertFetchResult.Empty
            },
            getAirQualityUseCase = mockk<GetAirQualityUseCase>().also { useCase ->
                coEvery { useCase.invoke(latitude = "31.23", longitude = "121.47") } returns
                    AirQualityFetchResult.UnsupportedRegion
            },
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state).isInstanceOf(WeatherDetailState.Content::class.java)
        val contentState = viewModel.uiState.value.state as WeatherDetailState.Content
        assertThat(contentState.alerts).isEmpty()
        assertThat(contentState.isAirQualityUnsupported).isTrue()
    }

    @Test
    fun init_whenAlertRequestFails_emitsPartialContentWithAlertsUnavailable() = runTest {
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf<String, Any?>(
                    WeatherForecastDestination.CITY_ID_ARG to "101020100",
                ),
            ),
            citiesFlow = MutableStateFlow(listOf(sampleCity(id = "101020100"))),
            getWeatherAlertsUseCase = mockk<GetWeatherAlertsUseCase>().also { useCase ->
                coEvery { useCase.invoke(latitude = "31.23", longitude = "121.47") } throws
                    IllegalStateException("401")
            },
            getAirQualityUseCase = mockk<GetAirQualityUseCase>().also { useCase ->
                coEvery { useCase.invoke(latitude = "31.23", longitude = "121.47") } returns
                    AirQualityFetchResult.Available(airQuality = sampleAirQuality())
            },
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state)
            .isInstanceOf(WeatherDetailState.PartialContent::class.java)
        val partialState = viewModel.uiState.value.state as WeatherDetailState.PartialContent
        assertThat(partialState.unavailableSections).containsExactly(WeatherDetailSection.Alerts)
        assertThat(partialState.airQuality?.aqi).isEqualTo("86")
    }

    @Test
    fun init_whenAirQualityRequestFails_emitsPartialContentWithAirQualityUnavailable() = runTest {
        val viewModel = createViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf<String, Any?>(
                    WeatherForecastDestination.CITY_ID_ARG to "101020100",
                ),
            ),
            citiesFlow = MutableStateFlow(listOf(sampleCity(id = "101020100"))),
            getWeatherAlertsUseCase = mockk<GetWeatherAlertsUseCase>().also { useCase ->
                coEvery { useCase.invoke(latitude = "31.23", longitude = "121.47") } returns
                    WeatherAlertFetchResult.Empty
            },
            getAirQualityUseCase = mockk<GetAirQualityUseCase>().also { useCase ->
                coEvery { useCase.invoke(latitude = "31.23", longitude = "121.47") } returns
                    AirQualityFetchResult.Failure(reason = AirQualityFailureReason.Timeout)
            },
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state)
            .isInstanceOf(WeatherDetailState.PartialContent::class.java)
        val partialState = viewModel.uiState.value.state as WeatherDetailState.PartialContent
        assertThat(partialState.unavailableSections).containsExactly(WeatherDetailSection.AirQuality)
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
        getWeatherAlertsUseCase: GetWeatherAlertsUseCase = mockk<GetWeatherAlertsUseCase>().also { useCase ->
            coEvery { useCase.invoke(any(), any()) } returns WeatherAlertFetchResult.Empty
        },
        getAirQualityUseCase: GetAirQualityUseCase = mockk<GetAirQualityUseCase>().also { useCase ->
            coEvery { useCase.invoke(any(), any()) } returns AirQualityFetchResult.UnsupportedRegion
        },
    ): WeatherDetailViewModel {
        val observeSavedCitiesUseCase = mockk<ObserveSavedCitiesUseCase>().also { useCase ->
            every { useCase.invoke() } returns citiesFlow
        }
        return WeatherDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeSavedCitiesUseCase = observeSavedCitiesUseCase,
            getWeatherAlertsUseCase = getWeatherAlertsUseCase,
            getAirQualityUseCase = getAirQualityUseCase,
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

    private fun sampleAlert(): WeatherAlert {
        return WeatherAlert(
            id = "10102010020260408120000",
            sender = "Shanghai Meteorological Center",
            publishTime = "2026-04-08T12:00+08:00",
            title = "Rainstorm Blue Warning",
            startTime = "2026-04-08T12:00+08:00",
            endTime = "2026-04-08T23:00+08:00",
            status = "active",
            severity = "Blue",
            severityColor = "Blue",
            type = "rainstorm",
            typeName = "Rainstorm",
            text = "Expect heavy rain in the next 6 hours.",
        )
    }

    private fun sampleAirQuality(): AirQuality {
        return AirQuality(
            publishTime = "2026-04-08T14:00+08:00",
            aqi = "86",
            category = "Moderate",
            primary = "pm2p5",
            pm2p5 = "65",
            pm10 = "72",
            no2 = "18",
            so2 = "5",
            co = "0.7",
            o3 = "45",
        )
    }
}
