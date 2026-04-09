package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndicesFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWeatherIndicesUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(
        locationId: String,
    ): WeatherIndicesFetchResult {
        return weatherRepository.fetchWeatherIndices(locationId = locationId)
    }
}
