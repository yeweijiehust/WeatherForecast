package io.github.yeweijiehust.weatherforecast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.yeweijiehust.weatherforecast.core.localization.AppLocaleViewModel
import io.github.yeweijiehust.weatherforecast.core.localization.WeatherForecastLocalizedContent
import io.github.yeweijiehust.weatherforecast.ui.theme.WeatherForecastTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appLocaleViewModel: AppLocaleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appLanguage by appLocaleViewModel.appLanguage.collectAsStateWithLifecycle()

            WeatherForecastLocalizedContent(language = appLanguage) {
                WeatherForecastTheme {
                    WeatherForecastApp()
                }
            }
        }
    }
}
