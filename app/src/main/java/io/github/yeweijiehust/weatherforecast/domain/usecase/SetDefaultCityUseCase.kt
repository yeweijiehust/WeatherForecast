package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import javax.inject.Inject

class SetDefaultCityUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {
    suspend operator fun invoke(cityId: String) {
        cityRepository.setDefaultCity(cityId)
    }
}
