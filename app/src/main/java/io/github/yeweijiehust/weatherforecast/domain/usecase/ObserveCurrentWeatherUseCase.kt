package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.CurrentWeather
import io.github.yeweijiehust.weatherforecast.domain.repository.WeatherRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCurrentWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
) {
    operator fun invoke(cityId: String): Flow<CurrentWeather?> {
        return weatherRepository.observeCurrentWeather(cityId)
    }
}
