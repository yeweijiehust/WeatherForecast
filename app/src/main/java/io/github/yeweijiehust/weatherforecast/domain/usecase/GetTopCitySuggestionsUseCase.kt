package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.City
import io.github.yeweijiehust.weatherforecast.domain.repository.CityRepository
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import javax.inject.Inject

class GetTopCitySuggestionsUseCase @Inject constructor(
    private val cityRepository: CityRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): List<City> {
        val settings = settingsRepository.getCurrentSettings()
        return cityRepository.fetchTopCities(language = settings.language.apiCode)
    }
}
