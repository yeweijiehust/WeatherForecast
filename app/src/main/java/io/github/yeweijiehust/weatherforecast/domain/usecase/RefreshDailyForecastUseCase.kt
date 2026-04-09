package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.repository.DailyForecastRepository
import javax.inject.Inject

class RefreshDailyForecastUseCase @Inject constructor(
    private val weatherRepository: DailyForecastRepository,
) {
    suspend operator fun invoke(
        cityId: String,
        forceRefresh: Boolean = false,
    ) {
        weatherRepository.refreshDailyForecast(
            cityId = cityId,
            forceRefresh = forceRefresh,
        )
    }
}
