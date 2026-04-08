package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWeatherAlertsUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(
        latitude: String,
        longitude: String,
    ): WeatherAlertFetchResult {
        return weatherRepository.fetchWeatherAlerts(
            latitude = latitude,
            longitude = longitude,
        )
    }
}
