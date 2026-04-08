package io.github.yeweijiehust.weatherforecast.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.yeweijiehust.weatherforecast.core.localization.WeatherForecastLocalizedContent
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.City
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
                        uiState = HomeUiState(defaultCity = null),
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
    fun contentState_showsDefaultCitySummary() {
        composeTestRule.setContent {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    HomeScreen(
                        uiState = HomeUiState(
                            defaultCity = City(
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
                        onManageCitiesClick = {},
                        onSettingsClick = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Current city").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shanghai").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shanghai, China").assertIsDisplayed()
    }
}
