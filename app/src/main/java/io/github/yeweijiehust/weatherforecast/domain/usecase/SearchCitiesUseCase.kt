package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(rawQuery: String): List<City> {
        val query = rawQuery.trim()
        if (query.isBlank()) {
            return emptyList()
        }
        val settings = settingsRepository.getCurrentSettings()

        return cityRepository.searchCities(
            query = query,
            language = settings.language.apiCode,
        )
    }
}
