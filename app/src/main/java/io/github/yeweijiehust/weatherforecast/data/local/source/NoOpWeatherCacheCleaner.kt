package io.github.yeweijiehust.weatherforecast.data.local.source

import javax.inject.Inject

class NoOpWeatherCacheCleaner @Inject constructor() : WeatherCacheCleaner {
    override suspend fun clearWeatherCache() = Unit
}
