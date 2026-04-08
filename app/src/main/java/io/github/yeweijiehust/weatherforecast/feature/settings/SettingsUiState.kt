package io.github.yeweijiehust.weatherforecast.feature.settings

import io.github.yeweijiehust.weatherforecast.domain.model.AppSettings

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
)

sealed interface SettingsEvent {
    data class ShowMessage(
        val message: String,
    ) : SettingsEvent
}
