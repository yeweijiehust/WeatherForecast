package io.github.yeweijiehust.weatherforecast.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.yeweijiehust.weatherforecast.core.localization.WeatherForecastLocalizedContent
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings
import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsScreenScreenshotTest {
    @Test
    fun defaultState_capturesSettingsLayout() {
        captureRoboImage(
            filePath = "build/outputs/roborazzi/settings-default.png",
        ) {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
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
        }
    }
}
