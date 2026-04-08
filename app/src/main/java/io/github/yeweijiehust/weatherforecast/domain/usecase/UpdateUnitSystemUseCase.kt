package io.github.yeweijiehust.weatherforecast.domain.usecase

import io.github.yeweijiehust.weatherforecast.domain.model.UnitSystem
import io.github.yeweijiehust.weatherforecast.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateUnitSystemUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(unitSystem: UnitSystem) {
        settingsRepository.updateUnitSystem(unitSystem)
    }
}
