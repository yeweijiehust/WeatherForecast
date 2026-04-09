package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.yeweijiehust.weatherforecast.core.localization.WeatherForecastLocalizedContent
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class HomeScreenScreenshotTest {
    @Test
    fun contentState_capturesCurrentWeatherLayout() {
        capture("home-current-weather.png", HomeState.Content(snapshot = sampleSnapshot()))
    }

    @Test
    fun loadingState_capturesLoadingLayout() {
        capture("home-loading.png", HomeState.Loading(city = sampleSnapshot().city))
    }

    @Test
    fun staleState_capturesStaleCacheLayout() {
        capture("home-stale-cache.png", HomeState.ContentWithStaleCache(snapshot = sampleSnapshot()))
    }

    @Test
    fun errorState_capturesNoCacheErrorLayout() {
        capture("home-error-no-cache.png", HomeState.ErrorNoCache(city = sampleSnapshot().city))
    }

    private fun capture(fileName: String, state: HomeState) {
        captureRoboImage(
            filePath = "build/outputs/roborazzi/$fileName",
        ) {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    HomeScreen(
                        uiState = HomeUiState(state = state),
                        onManageCitiesClick = {},
                        onOpenDetail = {},
                        onPullToRefresh = {},
                    )
                }
            }
        }
    }

    private fun sampleSnapshot(): HomeSnapshot {
        return HomeSnapshot(
            city = City(
                id = "101020100",
                name = "Shanghai",
                adm1 = "Shanghai",
                adm2 = "Shanghai",
                country = "China",
                lat = "31.23",
                lon = "121.47",
                timeZone = "Asia/Shanghai",
                isDefault = true,
            ),
            currentWeather = CurrentWeather(
                cityId = "101020100",
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
            ),
            hourlyForecast = listOf(
                HourlyForecast(
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
                ),
                HourlyForecast(
                    cityId = "101020100",
                    forecastTime = "2026-04-08T17:00+08:00",
                    temperature = "23",
                    conditionText = "Light Rain",
                    conditionIcon = "305",
                    precipitationProbability = "45",
                    precipitation = "0.4",
                    windDirection = "South",
                    windScale = "2",
                    windSpeed = "14",
                    fetchedAtEpochMillis = 100L,
                ),
            ),
            dailyForecast = listOf(
                DailyForecast(
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
                ),
                DailyForecast(
                    cityId = "101020100",
                    forecastDate = "2026-04-10",
                    tempMax = "28",
                    tempMin = "21",
                    conditionTextDay = "Partly Cloudy",
                    conditionIconDay = "101",
                    precipitationProbability = "25",
                    precipitation = "0.2",
                    windDirectionDay = "Southeast",
                    windScaleDay = "2",
                    windSpeedDay = "12",
                    fetchedAtEpochMillis = 100L,
                ),
            ),
            secondarySummary = HomeSecondarySummary(
                alerts = HomeAlertsSummary(
                    activeAlertCount = 2,
                    isUnavailable = false,
                ),
                airQuality = HomeAirQualitySummary(
                    aqi = "53",
                    category = "Good",
                    isUnsupportedRegion = false,
                    isUnavailable = false,
                ),
            ),
            lastUpdatedEpochMillis = 100L,
        )
    }
}
