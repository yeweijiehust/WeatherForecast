package io.github.yeweijiehust.weatherforecast.feature.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme
import org.junit.Assert.assertEquals
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
                    onSaveCity = {},
                    onSetDefaultCity = {},
                    onRemoveCity = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Sangamon, Illinois, United States").assertIsDisplayed()
        composeTestRule.onNodeWithText("Greene, Missouri, United States").assertIsDisplayed()
    }

    @Test
    fun savedCitiesState_showsDefaultMarkerAndActions() {
        composeTestRule.renderSearchScreen(
            uiState = CitySearchUiState(
                savedCities = listOf(
                    City(
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
                ),
            ),
        )

        composeTestRule.onNodeWithText("Saved cities").assertIsDisplayed()
        composeTestRule.onNodeWithText("Default").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remove").assertIsDisplayed()
    }

    @Test
    fun resultSaveAction_invokesSaveCallback() {
        var savedCityId: String? = null
        composeTestRule.renderSearchScreen(
            uiState = CitySearchUiState(
                resultState = CitySearchResultState.Results(
                    cities = listOf(
                        City(
                            id = "101020100",
                            name = "Shanghai",
                            adm1 = "Shanghai",
                            adm2 = "Shanghai",
                            country = "China",
                            lat = "31.23",
                            lon = "121.47",
                            timeZone = "Asia/Shanghai",
                        ),
                    ),
                ),
            ),
            onSaveCity = { savedCityId = it.id },
        )

        composeTestRule.onNodeWithText("Save").performClick()

        assertEquals("101020100", savedCityId)
    }

    private fun ComposeContentTestRule.renderSearchScreen(
        uiState: CitySearchUiState,
        onSaveCity: (City) -> Unit = {},
    ) {
        setContent {
            WeatherForecastTheme {
                CitySearchScreen(
                    uiState = uiState,
                    onQueryChanged = {},
                    onSearch = {},
                    onRetry = {},
                    onSaveCity = onSaveCity,
                    onSetDefaultCity = {},
                    onRemoveCity = {},
                )
            }
        }
    }
}
