package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.DailyForecast
import io.github.yeweijiehust.weatherforecast.domain.repository.DailyForecastRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveDailyForecastUseCase @Inject constructor(
    private val weatherRepository: DailyForecastRepository,
) {
    operator fun invoke(cityId: String): Flow<List<DailyForecast>> {
        return weatherRepository.observeDailyForecast(cityId)
    }
}
