package io.github.yeweijiehust.weatherforecast.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun screen_rendersSettingsSections() {
        composeTestRule.setContent {
            WeatherForecastTheme {
                SettingsScreen(
                    uiState = SettingsUiState(
                        settings = AppSettings(
                            language = AppLanguage.English,
                            unitSystem = UnitSystem.Metric,
                        ),
                    ),
                    onSelectLanguage = {},
                    onSelectUnitSystem = {},
                    onClearWeatherCache = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Units").assertIsDisplayed()
        composeTestRule.onNodeWithText("Language").assertIsDisplayed()
        composeTestRule.onNodeWithText("Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear cached weather").assertIsDisplayed()
    }

    @Test
    fun clearCacheButton_invokesCallback() {
        var clearInvoked = false
        composeTestRule.setContent {
            WeatherForecastTheme {
                SettingsScreen(
                    uiState = SettingsUiState(settings = AppSettings()),
                    onSelectLanguage = {},
                    onSelectUnitSystem = {},
                    onClearWeatherCache = { clearInvoked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Clear cached weather").performClick()

        assertTrue(clearInvoked)
    }
}
