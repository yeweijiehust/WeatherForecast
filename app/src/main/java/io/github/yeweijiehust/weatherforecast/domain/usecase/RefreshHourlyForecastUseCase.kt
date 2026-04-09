package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.repository.HourlyForecastRepository
import javax.inject.Inject

class RefreshHourlyForecastUseCase @Inject constructor(
    private val weatherRepository: HourlyForecastRepository,
) {
    suspend operator fun invoke(
        cityId: String,
        forceRefresh: Boolean = false,
    ) {
        weatherRepository.refreshHourlyForecast(
            cityId = cityId,
            forceRefresh = forceRefresh,
        )
    }
}
