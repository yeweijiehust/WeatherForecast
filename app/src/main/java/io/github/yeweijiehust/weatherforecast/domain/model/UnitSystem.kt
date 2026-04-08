package io.github.yeweijiehust.weatherforecast.domain.model

enum class UnitSystem(
    val storageValue: String,
    val apiCode: String,
    val displayName: String,
) {
    Metric(
        storageValue = "metric",
        apiCode = "m",
        displayName = "Metric",
    ),
    Imperial(
        storageValue = "imperial",
        apiCode = "i",
        displayName = "Imperial",
    ),
}
