package io.github.yeweijiehust.weatherforecast.domain.model

data class AirQuality(
    val publishTime: String,
    val aqi: String,
    val category: String,
    val primary: String,
    val pm2p5: String,
    val pm10: String,
    val no2: String,
    val so2: String,
    val co: String,
    val o3: String,
)

sealed interface AirQualityFetchResult {
    data class Available(
        val airQuality: AirQuality,
    ) : AirQualityFetchResult

    data object UnsupportedRegion : AirQualityFetchResult

    data class Failure(
        val reason: AirQualityFailureReason,
    ) : AirQualityFetchResult
}

enum class AirQualityFailureReason {
    Timeout,
    QuotaExceeded,
    Unauthorized,
    Unknown,
}
