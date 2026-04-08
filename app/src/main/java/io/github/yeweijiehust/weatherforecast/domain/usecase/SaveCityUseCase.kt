package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.model.SaveCityResult
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import javax.inject.Inject

class SaveCityUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {
    suspend operator fun invoke(city: City): SaveCityResult = cityRepository.saveCity(city)
}
