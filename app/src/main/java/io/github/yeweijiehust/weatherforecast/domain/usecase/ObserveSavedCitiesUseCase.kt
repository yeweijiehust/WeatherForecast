package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSavedCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository,
) {
    operator fun invoke(): Flow<List<City>> = cityRepository.observeSavedCities()
}
