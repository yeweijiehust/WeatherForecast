package io.github.yeweijiehust.weatherforecast.data.local.source

import javax.inject.Inject

class RoomWeatherCacheCleaner @Inject constructor(
    private val currentWeatherLocalDataSource: CurrentWeatherLocalDataSource,
    private val hourlyForecastLocalDataSource: HourlyForecastLocalDataSource,
) : WeatherCacheCleaner {
    override suspend fun clearWeatherCache() {
        currentWeatherLocalDataSource.clearCurrentWeatherCache()
        hourlyForecastLocalDataSource.clearHourlyForecastCache()
    }
}
