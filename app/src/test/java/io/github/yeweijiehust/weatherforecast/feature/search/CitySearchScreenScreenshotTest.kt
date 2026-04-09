package io.github.yeweijiehust.weatherforecast.feature.search

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.yeweijiehust.weatherforecast.core.localization.WeatherForecastLocalizedContent
import io.github.yeweijiehust.weatherforecast.domain.model.AppLanguage
import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CitySearchScreenScreenshotTest {
    @Test
    fun resultsState_capturesSearchLayout() {
        capture(
            fileName = "search-results.png",
            uiState = CitySearchUiState(
                query = "Shang",
                resultState = CitySearchResultState.Results(
                    cities = listOf(
                        city(
                            id = "101020100",
                            name = "Shanghai",
                            adm1 = "Shanghai",
                            adm2 = "Shanghai",
                            country = "China",
                            isDefault = false,
                        ),
                        city(
                            id = "101021200",
                            name = "Songjiang",
                            adm1 = "Shanghai",
                            adm2 = "Shanghai",
                            country = "China",
                            isDefault = false,
                        ),
                    ),
                ),
                savedCities = listOf(
                    city(
                        id = "101020100",
                        name = "Shanghai",
                        adm1 = "Shanghai",
                        adm2 = "Shanghai",
                        country = "China",
                        isDefault = true,
                    ),
                ),
                topCitySuggestions = listOf(
                    city(
                        id = "101020100",
                        name = "Shanghai",
                        adm1 = "Shanghai",
                        adm2 = "Shanghai",
                        country = "China",
                        isDefault = true,
                    ),
                    city(
                        id = "101010100",
                        name = "Beijing",
                        adm1 = "Beijing",
                        adm2 = "Beijing",
                        country = "China",
                        isDefault = false,
                    ),
                ),
            ),
        )
    }

    private fun capture(
        fileName: String,
        uiState: CitySearchUiState,
    ) {
        captureRoboImage(
            filePath = "build/outputs/roborazzi/$fileName",
        ) {
            WeatherForecastLocalizedContent(language = AppLanguage.English) {
                WeatherForecastTheme {
                    CitySearchScreen(
                        uiState = uiState,
                        onQueryChanged = {},
                        onSearch = {},
                        onRetry = {},
                        onSaveCity = {},
                        onUseTopCitySuggestion = {},
                        onSetDefaultCity = {},
                        onRemoveCity = {},
                    )
                }
            }
        }
    }

    private fun city(
        id: String,
        name: String,
        adm1: String,
        adm2: String,
        country: String,
        isDefault: Boolean,
    ): City {
        return City(
            id = id,
            name = name,
            adm1 = adm1,
            adm2 = adm2,
            country = country,
            lat = "31.23",
            lon = "121.47",
            timeZone = "Asia/Shanghai",
            isDefault = isDefault,
        )
    }
}
