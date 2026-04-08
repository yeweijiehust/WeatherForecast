package io.github.yeweijiehust.weatherforecast.core.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeatherForecastDestinationTest {
    @Test
    fun fromRoute_returnsMatchingDestination() {
        assertThat(WeatherForecastDestination.fromRoute("search"))
            .isEqualTo(WeatherForecastDestination.Search)
    }

    @Test
    fun fromRoute_returnsDetailForParameterizedDetailRoute() {
        assertThat(WeatherForecastDestination.fromRoute("detail/101020100"))
            .isEqualTo(WeatherForecastDestination.Detail)
    }

    @Test
    fun fromRoute_returnsDetailForDetailRoutePattern() {
        assertThat(
            WeatherForecastDestination.fromRoute(WeatherForecastDestination.Detail.route),
        ).isEqualTo(WeatherForecastDestination.Detail)
    }

    @Test
    fun fromRoute_returnsHomeForUnknownRoute() {
        assertThat(WeatherForecastDestination.fromRoute("unknown"))
            .isEqualTo(WeatherForecastDestination.Home)
    }
}
