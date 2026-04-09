package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherIndicesRepository
import javax.inject.Inject

class GetWeatherIndicesUseCase @Inject constructor(
    private val weatherRepository: WeatherIndicesRepository,
) {
    suspend operator fun invoke(
        locationId: String,
        forceRefresh: Boolean = false,
    ): WeatherIndicesFetchResult {
        return weatherRepository.fetchWeatherIndices(
            locationId = locationId,
            forceRefresh = forceRefresh,
        )
    }
}
