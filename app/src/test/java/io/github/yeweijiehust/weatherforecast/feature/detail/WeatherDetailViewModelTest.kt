package io.github.yeweijiehust.weatherforecast.feature.detail

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import io.github.yeweijiehust.weatherforecast.core.navigation.WeatherForecastDestination
import io.github.yeweijiehust.weatherforecast.domain.model.AirQuality
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationPoint
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationTimeline
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunset
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlert
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndex
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndices
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFailureReason
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFetchResult
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetAirQualityUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetMinutePrecipitationUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetSunriseSunsetUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetWeatherAlertsUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.GetWeatherIndicesUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveDailyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveHourlyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.ObserveSavedCitiesUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshDailyForecastUseCase
import io.github.yeweijiehust.weatherforecast.domain.usecase.RefreshHourlyForecastUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun init_withMatchingCityId_emitsContentWithForecastAlertsAndAirQuality() = runTest {
        val viewModel = createViewModel(
            citiesFlow = MutableStateFlow(listOf(sampleCity())),
            hourlyFlow = MutableStateFlow(listOf(sampleHourlyForecast())),
            dailyFlow = MutableStateFlow(listOf(sampleDailyForecast())),
            minutePrecipitationResult = MinutePrecipitationFetchResult.Available(
                timeline = sampleMinutePrecipitationTimeline(),
            ),
            sunriseSunsetResult = SunriseSunsetFetchResult.Available(
                sunriseSunset = sampleSunriseSunset(),
            ),
            weatherIndicesResult = WeatherIndicesFetchResult.Available(
                weatherIndices = sampleWeatherIndices(),
            ),
            alertResult = WeatherAlertFetchResult.Available(alerts = listOf(sampleAlert())),
            airQualityResult = AirQualityFetchResult.Available(airQuality = sampleAirQuality()),
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state).isInstanceOf(WeatherDetailState.Content::class.java)
        val content = viewModel.uiState.value.state as WeatherDetailState.Content
        assertThat(content.city.id).isEqualTo("101020100")
        assertThat(content.hourlyForecast).hasSize(1)
        assertThat(content.dailyForecast).hasSize(1)
        assertThat(content.minutePrecipitation?.points).hasSize(2)
        assertThat(content.isMinutePrecipitationUnsupported).isFalse()
        assertThat(content.sunriseSunset?.sunrise).isEqualTo("2026-04-09T05:34+08:00")
        assertThat(content.weatherIndices?.items).hasSize(2)
        assertThat(content.alerts).hasSize(1)
        assertThat(content.airQuality?.aqi).isEqualTo("86")
    }

    @Test
    fun init_withUnknownCityId_emitsErrorNoData() = runTest {
        val viewModel = createViewModel(
            citiesFlow = MutableStateFlow(listOf(sampleCity(id = "other"))),
            hourlyFlow = MutableStateFlow(emptyList()),
            dailyFlow = MutableStateFlow(emptyList()),
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state).isEqualTo(
            WeatherDetailState.ErrorNoData(cityId = "101020100"),
        )
    }

    @Test
    fun init_withNoAlertAndUnsupportedAqi_emitsContentWithSectionFallbacks() = runTest {
        val viewModel = createViewModel(
            citiesFlow = MutableStateFlow(listOf(sampleCity())),
            hourlyFlow = MutableStateFlow(listOf(sampleHourlyForecast())),
            dailyFlow = MutableStateFlow(listOf(sampleDailyForecast())),
            minutePrecipitationResult = MinutePrecipitationFetchResult.UnsupportedRegion,
            sunriseSunsetResult = SunriseSunsetFetchResult.Available(sampleSunriseSunset()),
            weatherIndicesResult = WeatherIndicesFetchResult.Empty,
            alertResult = WeatherAlertFetchResult.Empty,
            airQualityResult = AirQualityFetchResult.UnsupportedRegion,
        )

        dispatcher.scheduler.advanceUntilIdle()

        val content = viewModel.uiState.value.state as WeatherDetailState.Content
        assertThat(content.alerts).isEmpty()
        assertThat(content.minutePrecipitation).isNull()
        assertThat(content.isMinutePrecipitationUnsupported).isTrue()
        assertThat(content.sunriseSunset?.sunset).isEqualTo("2026-04-09T18:18+08:00")
        assertThat(content.weatherIndices).isNull()
        assertThat(content.airQuality).isNull()
        assertThat(content.isAirQualityUnsupported).isTrue()
    }

    @Test
    fun init_whenMinutePrecipitationRequestFails_emitsPartialContentWithMinutelyUnavailable() = runTest {
        val viewModel = createViewModel(
            citiesFlow = MutableStateFlow(listOf(sampleCity())),
            hourlyFlow = MutableStateFlow(listOf(sampleHourlyForecast())),
            dailyFlow = MutableStateFlow(listOf(sampleDailyForecast())),
            minutePrecipitationResult = MinutePrecipitationFetchResult.Failure(
                reason = MinutePrecipitationFailureReason.Timeout,
            ),
            sunriseSunsetResult = SunriseSunsetFetchResult.Available(sampleSunriseSunset()),
            weatherIndicesResult = WeatherIndicesFetchResult.Empty,
            alertResult = WeatherAlertFetchResult.Empty,
            airQualityResult = AirQualityFetchResult.UnsupportedRegion,
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state)
            .isInstanceOf(WeatherDetailState.PartialContent::class.java)
        val partial = viewModel.uiState.value.state as WeatherDetailState.PartialContent
        assertThat(partial.unavailableSections).contains(WeatherDetailSection.MinutePrecipitation)
    }

    @Test
    fun init_whenAstronomyRequestFails_emitsPartialContentWithAstronomyUnavailable() = runTest {
        val viewModel = createViewModel(
            citiesFlow = MutableStateFlow(listOf(sampleCity())),
            hourlyFlow = MutableStateFlow(listOf(sampleHourlyForecast())),
            dailyFlow = MutableStateFlow(listOf(sampleDailyForecast())),
            minutePrecipitationResult = MinutePrecipitationFetchResult.UnsupportedRegion,
            sunriseSunsetResult = SunriseSunsetFetchResult.Failure(
                reason = SunriseSunsetFailureReason.Timeout,
            ),
            weatherIndicesResult = WeatherIndicesFetchResult.Empty,
            alertResult = WeatherAlertFetchResult.Empty,
            airQualityResult = AirQualityFetchResult.UnsupportedRegion,
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state)
            .isInstanceOf(WeatherDetailState.PartialContent::class.java)
        val partial = viewModel.uiState.value.state as WeatherDetailState.PartialContent
        assertThat(partial.unavailableSections).contains(WeatherDetailSection.Astronomy)
    }

    @Test
    fun init_whenIndicesRequestFails_emitsPartialContentWithIndicesUnavailable() = runTest {
        val viewModel = createViewModel(
            citiesFlow = MutableStateFlow(listOf(sampleCity())),
            hourlyFlow = MutableStateFlow(listOf(sampleHourlyForecast())),
            dailyFlow = MutableStateFlow(listOf(sampleDailyForecast())),
            minutePrecipitationResult = MinutePrecipitationFetchResult.UnsupportedRegion,
            sunriseSunsetResult = SunriseSunsetFetchResult.Available(sampleSunriseSunset()),
            weatherIndicesResult = WeatherIndicesFetchResult.Failure(
                reason = WeatherIndicesFailureReason.Timeout,
            ),
            alertResult = WeatherAlertFetchResult.Empty,
            airQualityResult = AirQualityFetchResult.UnsupportedRegion,
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state)
            .isInstanceOf(WeatherDetailState.PartialContent::class.java)
        val partial = viewModel.uiState.value.state as WeatherDetailState.PartialContent
        assertThat(partial.unavailableSections).contains(WeatherDetailSection.Indices)
    }

    @Test
    fun init_whenAlertRequestFails_emitsPartialContentWithAlertsUnavailable() = runTest {
        val viewModel = createViewModel(
            citiesFlow = MutableStateFlow(listOf(sampleCity())),
            hourlyFlow = MutableStateFlow(listOf(sampleHourlyForecast())),
            dailyFlow = MutableStateFlow(listOf(sampleDailyForecast())),
            alertFailure = IllegalStateException("401"),
            airQualityResult = AirQualityFetchResult.Available(sampleAirQuality()),
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state)
            .isInstanceOf(WeatherDetailState.PartialContent::class.java)
        val partial = viewModel.uiState.value.state as WeatherDetailState.PartialContent
        assertThat(partial.unavailableSections).contains(WeatherDetailSection.Alerts)
        assertThat(partial.airQuality?.aqi).isEqualTo("86")
    }

    @Test
    fun init_whenAirQualityRequestFails_emitsPartialContentWithAirQualityUnavailable() = runTest {
        val viewModel = createViewModel(
            citiesFlow = MutableStateFlow(listOf(sampleCity())),
            hourlyFlow = MutableStateFlow(listOf(sampleHourlyForecast())),
            dailyFlow = MutableStateFlow(listOf(sampleDailyForecast())),
            alertResult = WeatherAlertFetchResult.Empty,
            airQualityResult = AirQualityFetchResult.Failure(
                reason = AirQualityFailureReason.Timeout,
            ),
        )

        dispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.state)
            .isInstanceOf(WeatherDetailState.PartialContent::class.java)
        val partial = viewModel.uiState.value.state as WeatherDetailState.PartialContent
        assertThat(partial.unavailableSections).contains(WeatherDetailSection.AirQuality)
    }

    @Test
    fun retryHourlySection_triggersRefreshAgain() = runTest {
        val refreshHourly = mockk<RefreshHourlyForecastUseCase>(relaxed = true)
        val viewModel = createViewModel(
            citiesFlow = MutableStateFlow(listOf(sampleCity())),
            hourlyFlow = MutableStateFlow(listOf(sampleHourlyForecast())),
            dailyFlow = MutableStateFlow(listOf(sampleDailyForecast())),
            weatherIndicesResult = WeatherIndicesFetchResult.Empty,
            refreshHourlyForecastUseCase = refreshHourly,
        )

        dispatcher.scheduler.advanceUntilIdle()
        viewModel.retryHourlySection()
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 2) { refreshHourly.invoke("101020100") }
    }

    private fun createViewModel(
        citiesFlow: MutableStateFlow<List<City>>,
        hourlyFlow: MutableStateFlow<List<HourlyForecast>>,
        dailyFlow: MutableStateFlow<List<DailyForecast>>,
        minutePrecipitationResult: MinutePrecipitationFetchResult = MinutePrecipitationFetchResult.UnsupportedRegion,
        sunriseSunsetResult: SunriseSunsetFetchResult = SunriseSunsetFetchResult.Available(sampleSunriseSunset()),
        weatherIndicesResult: WeatherIndicesFetchResult = WeatherIndicesFetchResult.Empty,
        alertResult: WeatherAlertFetchResult = WeatherAlertFetchResult.Empty,
        airQualityResult: AirQualityFetchResult = AirQualityFetchResult.UnsupportedRegion,
        alertFailure: Throwable? = null,
        refreshHourlyForecastUseCase: RefreshHourlyForecastUseCase = mockk(relaxed = true),
        refreshDailyForecastUseCase: RefreshDailyForecastUseCase = mockk(relaxed = true),
    ): WeatherDetailViewModel {
        val observeSavedCitiesUseCase = mockk<ObserveSavedCitiesUseCase>().also { useCase ->
            every { useCase.invoke() } returns citiesFlow
        }
        val observeHourlyForecastUseCase = mockk<ObserveHourlyForecastUseCase>().also { useCase ->
            every { useCase.invoke("101020100") } returns hourlyFlow
        }
        val observeDailyForecastUseCase = mockk<ObserveDailyForecastUseCase>().also { useCase ->
            every { useCase.invoke("101020100") } returns dailyFlow
        }
        val getWeatherAlertsUseCase = mockk<GetWeatherAlertsUseCase>().also { useCase ->
            if (alertFailure != null) {
                coEvery {
                    useCase.invoke(latitude = "31.23", longitude = "121.47")
                } throws alertFailure
            } else {
                coEvery {
                    useCase.invoke(latitude = "31.23", longitude = "121.47")
                } returns alertResult
            }
        }
        val getMinutePrecipitationUseCase = mockk<GetMinutePrecipitationUseCase>().also { useCase ->
            coEvery {
                useCase.invoke(latitude = "31.23", longitude = "121.47")
            } returns minutePrecipitationResult
        }
        val astronomyDate = LocalDate
            .now(ZoneId.of("Asia/Shanghai"))
            .format(DateTimeFormatter.BASIC_ISO_DATE)
        val getSunriseSunsetUseCase = mockk<GetSunriseSunsetUseCase>().also { useCase ->
            coEvery {
                useCase.invoke(locationId = "101020100", date = astronomyDate)
            } returns sunriseSunsetResult
        }
        val getWeatherIndicesUseCase = mockk<GetWeatherIndicesUseCase>().also { useCase ->
            coEvery {
                useCase.invoke(locationId = "101020100")
            } returns weatherIndicesResult
        }
        val getAirQualityUseCase = mockk<GetAirQualityUseCase>().also { useCase ->
            coEvery {
                useCase.invoke(latitude = "31.23", longitude = "121.47")
            } returns airQualityResult
        }
        return WeatherDetailViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf<String, Any?>(WeatherForecastDestination.CITY_ID_ARG to "101020100"),
            ),
            observeSavedCitiesUseCase = observeSavedCitiesUseCase,
            observeHourlyForecastUseCase = observeHourlyForecastUseCase,
            observeDailyForecastUseCase = observeDailyForecastUseCase,
            refreshHourlyForecastUseCase = refreshHourlyForecastUseCase,
            refreshDailyForecastUseCase = refreshDailyForecastUseCase,
            getWeatherAlertsUseCase = getWeatherAlertsUseCase,
            getAirQualityUseCase = getAirQualityUseCase,
            getMinutePrecipitationUseCase = getMinutePrecipitationUseCase,
            getSunriseSunsetUseCase = getSunriseSunsetUseCase,
            getWeatherIndicesUseCase = getWeatherIndicesUseCase,
        )
    }

    private fun sampleCity(id: String = "101020100"): City {
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

    private fun sampleHourlyForecast(): HourlyForecast {
        return HourlyForecast(
            cityId = "101020100",
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

    private fun sampleDailyForecast(): DailyForecast {
        return DailyForecast(
            cityId = "101020100",
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

    private fun sampleMinutePrecipitationTimeline(): MinutePrecipitationTimeline {
        return MinutePrecipitationTimeline(
            updateTime = "2026-04-09T14:00+08:00",
            summary = "Rain expected in 30 minutes.",
            points = listOf(
                MinutePrecipitationPoint(
                    forecastTime = "2026-04-09T14:05+08:00",
                    precipitation = "0.0",
                    type = "rain",
                ),
                MinutePrecipitationPoint(
                    forecastTime = "2026-04-09T14:10+08:00",
                    precipitation = "0.2",
                    type = "rain",
                ),
            ),
        )
    }

    private fun sampleSunriseSunset(): SunriseSunset {
        return SunriseSunset(
            updateTime = "2026-04-09T11:00+08:00",
            sunrise = "2026-04-09T05:34+08:00",
            sunset = "2026-04-09T18:18+08:00",
        )
    }

    private fun sampleWeatherIndices(): WeatherIndices {
        return WeatherIndices(
            updateTime = "2026-04-09T13:57+08:00",
            items = listOf(
                WeatherIndex(
                    date = "2026-04-09",
                    type = "5",
                    name = "UV Index",
                    level = "2",
                    category = "Low",
                    text = "Use basic sunscreen.",
                ),
                WeatherIndex(
                    date = "2026-04-09",
                    type = "8",
                    name = "Comfort",
                    level = "2",
                    category = "Good",
                    text = "Feels comfortable.",
                ),
            ),
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
