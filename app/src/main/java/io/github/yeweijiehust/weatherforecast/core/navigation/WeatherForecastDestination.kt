package io.github.yeweijiehust.weatherforecast.core.navigation

import androidx.annotation.StringRes
import io.github.yeweijiehust.weatherforecast.R

enum class WeatherForecastDestination(
    val route: String,
    @StringRes val titleResId: Int,
) {
    Home(route = "home", titleResId = R.string.title_home),
    Search(route = "search", titleResId = R.string.title_search),
    Settings(route = "settings", titleResId = R.string.title_settings);

    companion object {
        fun fromRoute(route: String?): WeatherForecastDestination {
            return entries.firstOrNull { it.route == route } ?: Home
        }
    }
}
