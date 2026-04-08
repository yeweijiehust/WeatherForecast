package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.HourlyForecast
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveHourlyForecastUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    operator fun invoke(cityId: String): Flow<List<HourlyForecast>> {
        return weatherRepository.observeHourlyForecast(cityId)
    }
}
