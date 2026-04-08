package io.github.yeweijiehust.weatherforecast.domain.model

enum class UnitSystem(
    val storageValue: String,
    val apiCode: String,
) {
    Metric(
        storageValue = "metric",
        apiCode = "m",
    ),
    Imperial(
        storageValue = "imperial",
        apiCode = "i",
    ),
}
