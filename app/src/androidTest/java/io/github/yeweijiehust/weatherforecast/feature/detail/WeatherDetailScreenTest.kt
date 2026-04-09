package io.github.yeweijiehust.weatherforecast.feature.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import io.github.yeweijiehust.weatherforecast.core.localization.WeatherForecastLocalizedContent
import io.github.yeweijiehust.weatherforecast.domain.model.AirQuality
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationPoint
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationTimeline
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunset
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlert
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme
import org.junit.Rule
import org.junit.Test

class WeatherDetailScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsLoadingMessage() {
        renderScreen(WeatherDetailUiState(state = WeatherDetailState.Loading))

        composeTestRule.onNodeWithText("Loading weather details…").assertIsDisplayed()
    }

    @Test
    fun contentState_showsForecastAlertsAndAirQualitySections() {
        renderScreen(
            WeatherDetailUiState(
                state = WeatherDetailState.Content(
                    city = sampleCity(),
                    hourlyForecast = listOf(sampleHourlyForecast()),
                    dailyForecast = listOf(sampleDailyForecast()),
                    minutePrecipitation = sampleMinutePrecipitationTimeline(),
                    sunriseSunset = sampleSunriseSunset(),
                    alerts = listOf(sampleAlert()),
                    airQuality = sampleAirQuality(),
                ),
            ),
        )

        composeTestRule.onNodeWithText("Weather detail for Shanghai").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hourly timeline").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daily forecast").assertIsDisplayed()
        composeTestRule.onNodeWithText("Minute precipitation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Summary: Rain expected in 30 minutes.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sunrise and sunset").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sunrise: 2026-04-09T05:34+08:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sunset: 2026-04-09T18:18+08:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weather alerts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rainstorm Blue Warning").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 active alert(s)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Air quality").assertIsDisplayed()
        composeTestRule.onNodeWithText("AQI 86 (Moderate)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Primary pollutant: pm2p5").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Pollutant breakdown").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("PM2.5").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("65").assertCountEquals(1)
    }

    @Test
    fun partialContentState_showsSectionErrorWithRetry() {
        renderScreen(
            WeatherDetailUiState(
                state = WeatherDetailState.PartialContent(
                    city = sampleCity(),
                    hourlyForecast = emptyList(),
                    dailyForecast = listOf(sampleDailyForecast()),
                    alerts = emptyList(),
                    airQuality = null,
                    isAirQualityUnsupported = false,
                    minutePrecipitation = null,
                    isMinutePrecipitationUnsupported = false,
                    sunriseSunset = null,
                    unavailableSections = setOf(
                        WeatherDetailSection.HourlyForecast,
                        WeatherDetailSection.MinutePrecipitation,
                        WeatherDetailSection.Astronomy,
                        WeatherDetailSection.AirQuality,
                    ),
                ),
            ),
        )

        composeTestRule.onNodeWithText("Some detail datasets are currently unavailable.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Hourly forecast is temporarily unavailable.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Minute precipitation data is temporarily unavailable.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Sunrise and sunset data is temporarily unavailable.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Air quality data is temporarily unavailable.")
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Retry").onFirst().assertIsDisplayed()
    }

    @Test
    fun contentState_showsUnsupportedMessageWhenMinutelyNotSupported() {
        renderScreen(
            WeatherDetailUiState(
                state = WeatherDetailState.Content(
                    city = sampleCity(),
                    hourlyForecast = listOf(sampleHourlyForecast()),
                    dailyForecast = listOf(sampleDailyForecast()),
                    minutePrecipitation = null,
                    isMinutePrecipitationUnsupported = true,
                    alerts = emptyList(),
                    airQuality = null,
                    isAirQualityUnsupported = true,
                ),
            ),
        )

        composeTestRule.onNodeWithText("Minute precipitation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Minute precipitation is not supported for this region.")
            .assertIsDisplayed()
    }

    private fun renderScreen(uiState: WeatherDetailUiState) {
        composeTestRule.setContent {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    WeatherDetailScreen(
                        uiState = uiState,
                        onRetryHourly = {},
                        onRetryDaily = {},
                        onRetryMinutePrecipitation = {},
                        onRetryAstronomy = {},
                        onRetryAlerts = {},
                        onRetryAirQuality = {},
                        onRetryAll = {},
                    )
                }
            }
        }
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
