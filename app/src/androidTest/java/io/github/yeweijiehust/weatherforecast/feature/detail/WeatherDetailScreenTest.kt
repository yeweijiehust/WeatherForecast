package io.github.yeweijiehust.weatherforecast.feature.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.yeweijiehust.weatherforecast.core.localization.WeatherForecastLocalizedContent
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme
import org.junit.Rule
import org.junit.Test

class WeatherDetailScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsLoadingMessage() {
        composeTestRule.setContent {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    WeatherDetailScreen(
                        uiState = WeatherDetailUiState(state = WeatherDetailState.Loading),
                        onRetry = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Loading weather details…").assertIsDisplayed()
    }

    @Test
    fun contentState_showsCityHeader() {
        composeTestRule.setContent {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    WeatherDetailScreen(
                        uiState = WeatherDetailUiState(
                            state = WeatherDetailState.Content(city = sampleCity()),
                        ),
                        onRetry = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Weather detail for Shanghai").assertIsDisplayed()
        composeTestRule.onNodeWithText("Post-MVP detail sections will be enabled step by step.")
            .assertIsDisplayed()
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
}
