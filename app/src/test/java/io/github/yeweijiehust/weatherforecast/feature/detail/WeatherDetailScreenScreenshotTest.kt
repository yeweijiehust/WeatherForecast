package io.github.yeweijiehust.weatherforecast.feature.detail

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.yeweijiehust.weatherforecast.core.localization.WeatherForecastLocalizedContent
import io.github.yeweijiehust.weatherforecast.domain.model.AirQuality
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationPoint
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationTimeline
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlert
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class WeatherDetailScreenScreenshotTest {
    @Test
    fun contentState_capturesDetailLayout() {
        capture(
            "detail-content.png",
            WeatherDetailState.Content(
                city = sampleCity(),
                hourlyForecast = listOf(sampleHourlyForecast()),
                dailyForecast = listOf(sampleDailyForecast()),
                minutePrecipitation = sampleMinutePrecipitationTimeline(),
                alerts = listOf(sampleAlert()),
                airQuality = sampleAirQuality(),
            ),
        )
    }

    @Test
    fun partialState_capturesSectionFallbackLayout() {
        capture(
            "detail-partial.png",
            WeatherDetailState.PartialContent(
                city = sampleCity(),
                hourlyForecast = emptyList(),
                dailyForecast = listOf(sampleDailyForecast()),
                minutePrecipitation = null,
                isMinutePrecipitationUnsupported = false,
                alerts = emptyList(),
                airQuality = null,
                isAirQualityUnsupported = false,
                unavailableSections = setOf(
                    WeatherDetailSection.HourlyForecast,
                    WeatherDetailSection.MinutePrecipitation,
                    WeatherDetailSection.AirQuality,
                ),
            ),
        )
    }

    @Test
    fun errorState_capturesNoDataLayout() {
        capture(
            "detail-error.png",
            WeatherDetailState.ErrorNoData(cityId = "101020100"),
        )
    }

    private fun capture(fileName: String, state: WeatherDetailState) {
        captureRoboImage(
            filePath = "build/outputs/roborazzi/$fileName",
        ) {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    WeatherDetailScreen(
                        uiState = WeatherDetailUiState(state = state),
                        onRetryHourly = {},
                        onRetryDaily = {},
                        onRetryMinutePrecipitation = {},
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
