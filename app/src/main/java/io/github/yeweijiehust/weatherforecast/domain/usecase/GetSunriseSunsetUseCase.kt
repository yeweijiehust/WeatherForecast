package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunsetFetchResult
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import javax.inject.Inject

class GetSunriseSunsetUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    suspend operator fun invoke(
        locationId: String,
        date: String,
        forceRefresh: Boolean = false,
    ): SunriseSunsetFetchResult {
        return weatherRepository.fetchSunriseSunset(
            locationId = locationId,
            date = date,
            forceRefresh = forceRefresh,
        )
    }
}
