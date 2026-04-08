package io.github.yeweijiehust.weatherforecast.core.navigation

enum class WeatherForecastDestination(
    val route: String,
    val title: String,
) {
    Home(route = "home", title = "Home"),
    Search(route = "search", title = "Manage Cities"),
    Settings(route = "settings", title = "Settings");

    companion object {
        fun fromRoute(route: String?): WeatherForecastDestination {
            return entries.firstOrNull { it.route == route } ?: Home
        }
    }
}
