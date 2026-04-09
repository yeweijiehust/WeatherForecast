package io.github.yeweijiehust.weatherforecast.domain.model

data class AppSettings(
    val language: AppLanguage = AppLanguage.SimplifiedChinese,
    val unitSystem: UnitSystem = UnitSystem.Metric,
)
