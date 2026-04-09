package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlertFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherAlertsRepository
import javax.inject.Inject

class GetWeatherAlertsUseCase @Inject constructor(
    private val weatherRepository: WeatherAlertsRepository,
) {
    suspend operator fun invoke(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean = false,
    ): WeatherAlertFetchResult {
        return weatherRepository.fetchWeatherAlerts(
            latitude = latitude,
            longitude = longitude,
            forceRefresh = forceRefresh,
        )
    }
}
