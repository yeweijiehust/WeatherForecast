package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import javax.inject.Inject

class RefreshDailyForecastUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
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
