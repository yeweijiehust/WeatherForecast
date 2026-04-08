package io.github.yeweijiehust.weatherforecast.core.navigation

import android.net.Uri
import androidx.annotation.StringRes
import io.github.yeweijiehust.weatherforecast.R

enum class WeatherForecastDestination(
    val route: String,
    @StringRes val titleResId: Int,
    val isTopLevel: Boolean = true,
) {
    Home(route = "home", titleResId = R.string.title_home),
    Search(route = "search", titleResId = R.string.title_search),
    Settings(route = "settings", titleResId = R.string.title_settings),
    Detail(
        route = "detail/{cityId}",
        titleResId = R.string.title_detail,
        isTopLevel = false,
    );

    val cityIdArg: String
        get() = CITY_ID_ARG

    fun routeForCity(cityId: String): String {
        return "detail/${Uri.encode(cityId)}"
    }

    companion object {
        const val CITY_ID_ARG = "cityId"
        private const val DETAIL_ROUTE_PREFIX = "detail/"

        fun fromRoute(route: String?): WeatherForecastDestination {
            if (route == null) return Home
            if (route == Detail.route || route.startsWith(DETAIL_ROUTE_PREFIX)) {
                return Detail
            }
            return entries.firstOrNull { it.route == route } ?: Home
        }
    }
}
