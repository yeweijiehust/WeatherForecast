package io.github.yeweijiehust.weatherforecast.domain.model

data class WeatherAlert(
    val id: String,
    val sender: String,
    val publishTime: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val severity: String,
    val severityColor: String,
    val type: String,
    val typeName: String,
    val text: String,
)

sealed interface WeatherAlertFetchResult {
    data class Available(
        val alerts: List<WeatherAlert>,
    ) : WeatherAlertFetchResult

    data object Empty : WeatherAlertFetchResult
}
