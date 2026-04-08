package io.github.yeweijiehust.weatherforecast.data.local.source

interface WeatherCacheCleaner {
    suspend fun clearWeatherCache()
}
