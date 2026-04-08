package io.github.yeweijiehust.weatherforecast.feature.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme
import org.junit.Rule
import org.junit.Test

class CitySearchScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun resultsState_rendersDisambiguatedCities() {
        composeTestRule.setContent {
            WeatherForecastTheme {
                CitySearchScreen(
                    uiState = CitySearchUiState(
                        query = "Springfield",
                        resultState = CitySearchResultState.Results(
                            cities = listOf(
                                City(
                                    id = "1",
                                    name = "Springfield",
                                    adm1 = "Illinois",
                                    adm2 = "Sangamon",
                                    country = "United States",
                                    lat = "39.78",
                                    lon = "-89.64",
                                    timeZone = "America/Chicago",
                                ),
                                City(
                                    id = "2",
                                    name = "Springfield",
                                    adm1 = "Missouri",
                                    adm2 = "Greene",
                                    country = "United States",
                                    lat = "37.21",
                                    lon = "-93.29",
                                    timeZone = "America/Chicago",
                                ),
                            ),
                        ),
                    ),
                    onQueryChanged = {},
                    onSearch = {},
                    onRetry = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Sangamon, Illinois, United States").assertIsDisplayed()
        composeTestRule.onNodeWithText("Greene, Missouri, United States").assertIsDisplayed()
    }
}
