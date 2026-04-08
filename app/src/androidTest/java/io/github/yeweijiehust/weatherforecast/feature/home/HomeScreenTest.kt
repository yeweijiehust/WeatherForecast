package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.yeweijiehust.weatherforecast.core.localization.WeatherForecastLocalizedContent
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme
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
                        onSettingsClick = {},
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
                            state = HomeState.Content(
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
                            ),
                        ),
                        onManageCitiesClick = {},
                        onSettingsClick = {},
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
    }
}
