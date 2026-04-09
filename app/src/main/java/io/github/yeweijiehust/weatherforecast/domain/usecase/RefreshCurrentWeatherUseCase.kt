package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.repository.CurrentWeatherRepository
import javax.inject.Inject

class RefreshCurrentWeatherUseCase @Inject constructor(
    private val weatherRepository: CurrentWeatherRepository,
) {
    suspend operator fun invoke(
        cityId: String,
        forceRefresh: Boolean = false,
    ) {
        weatherRepository.refreshCurrentWeather(
            cityId = cityId,
            forceRefresh = forceRefresh,
        )
    }
}
