package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.yeweijiehust.weatherforecast.core.localization.WeatherForecastLocalizedContent
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_promptsUserToAddFirstCity() {
        composeTestRule.setContent {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    HomeScreen(
                        uiState = HomeUiState(state = HomeState.EmptyNoCity),
                        onManageCitiesClick = {},
                        onOpenDetail = {},
                        onPullToRefresh = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("No city selected yet.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add your first city").assertIsDisplayed()
    }

    @Test
    fun contentState_showsCurrentWeatherSummary() {
        composeTestRule.setContent {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    HomeScreen(
                        uiState = HomeUiState(
                            state = HomeState.Content(snapshot = sampleSnapshot()),
                        ),
                        onManageCitiesClick = {},
                        onOpenDetail = {},
                        onPullToRefresh = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Shanghai").assertIsDisplayed()
        composeTestRule.onNodeWithText("26°").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sunny").assertIsDisplayed()
        composeTestRule.onNodeWithText("Feels like 28°").assertIsDisplayed()
        composeTestRule.onNodeWithText("Humidity").assertIsDisplayed()
        composeTestRule.onNodeWithText("65%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next 24 Hours").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cloudy").assertIsDisplayed()
        composeTestRule.onNodeWithText("POP 20%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next 7 Days").assertIsDisplayed()
        composeTestRule.onNodeWithText("Last updated", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Quick Insights").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 active alert(s)").assertIsDisplayed()
        composeTestRule.onNodeWithText("AQI 53 (Good)").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Pollutant breakdown").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Primary pollutant:", substring = true).assertCountEquals(0)
        composeTestRule.onAllNodesWithText("Rainstorm Blue Warning").assertCountEquals(0)
    }

    @Test
    fun contentState_formatsDailyDateForChineseLocale() {
        composeTestRule.setContent {
            WeatherForecastLocalizedContent(language = AppLanguage.SimplifiedChinese) {
                WeatherForecastTheme {
                    HomeScreen(
                        uiState = HomeUiState(
                            state = HomeState.Content(snapshot = sampleSnapshot()),
                        ),
                        onManageCitiesClick = {},
                        onOpenDetail = {},
                        onPullToRefresh = {},
                    )
                }
            }
        }

        composeTestRule.onAllNodesWithText("4月9日", substring = true).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Thu, Apr", substring = true).assertCountEquals(0)
    }

    @Test
    fun refreshingState_keepsContentVisibleAndShowsRefreshingLabel() {
        composeTestRule.setContent {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    HomeScreen(
                        uiState = HomeUiState(
                            state = HomeState.Refreshing(snapshot = sampleSnapshot()),
                        ),
                        onManageCitiesClick = {},
                        onOpenDetail = {},
                        onPullToRefresh = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Refreshing weather…").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shanghai").assertIsDisplayed()
    }

    @Test
    fun staleState_showsCacheFallbackLabel() {
        composeTestRule.setContent {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    HomeScreen(
                        uiState = HomeUiState(
                            state = HomeState.ContentWithStaleCache(snapshot = sampleSnapshot()),
                        ),
                        onManageCitiesClick = {},
                        onOpenDetail = {},
                        onPullToRefresh = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Refresh failed. Showing cached data.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shanghai").assertIsDisplayed()
    }

    @Test
    fun errorState_showsRetryAction() {
        composeTestRule.setContent {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    HomeScreen(
                        uiState = HomeUiState(
                            state = HomeState.ErrorNoCache(
                                city = sampleSnapshot().city,
                            ),
                        ),
                        onManageCitiesClick = {},
                        onOpenDetail = {},
                        onPullToRefresh = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        composeTestRule.onNodeWithText("We couldn't load current weather for this city.").assertIsDisplayed()
    }

    @Test
    fun contentState_clickViewDetailsCallsOnOpenDetailWithCityId() {
        var openedCityId: String? = null

        composeTestRule.setContent {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    HomeScreen(
                        uiState = HomeUiState(
                            state = HomeState.Content(snapshot = sampleSnapshot()),
                        ),
                        onManageCitiesClick = {},
                        onOpenDetail = { cityId -> openedCityId = cityId },
                        onPullToRefresh = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("View details").performClick()

        assertEquals("101020100", openedCityId)
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
            ),
            dailyForecast = listOf(
                DailyForecast(
                    cityId = "101020100",
                    forecastDate = "2026-04-09",
                    tempMax = "30",
                    tempMin = "22",
                    conditionTextDay = "Windy",
                    conditionIconDay = "100",
                    precipitationProbability = "10",
                    precipitation = "0.0",
                    windDirectionDay = "South",
                    windScaleDay = "3",
                    windSpeedDay = "16",
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
