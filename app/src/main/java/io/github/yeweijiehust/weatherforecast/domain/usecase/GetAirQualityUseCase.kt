package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.AirQualityFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.AirQualityRepository
import javax.inject.Inject

class GetAirQualityUseCase @Inject constructor(
    private val weatherRepository: AirQualityRepository,
) {
    suspend operator fun invoke(
        latitude: String,
        longitude: String,
        forceRefresh: Boolean = false,
    ): AirQualityFetchResult {
        return weatherRepository.fetchAirQuality(
            latitude = latitude,
            longitude = longitude,
            forceRefresh = forceRefresh,
        )
    }
}
