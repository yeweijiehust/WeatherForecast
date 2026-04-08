package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SearchLanguageProvider
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository,
    private val searchLanguageProvider: SearchLanguageProvider,
) {
    suspend operator fun invoke(rawQuery: String): List<City> {
        val query = rawQuery.trim()
        if (query.isBlank()) {
            return emptyList()
        }

        return cityRepository.searchCities(
            query = query,
            language = searchLanguageProvider.currentLanguage(),
        )
    }
}
