package io.github.yeweijiehust.weatherforecast.domain.model

data class AppSettings(
    val language: AppLanguage = AppLanguage.English,
    val unitSystem: UnitSystem = UnitSystem.Metric,
)
