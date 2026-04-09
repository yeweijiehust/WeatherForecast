package io.github.yeweijiehust.weatherforecast.domain.model

data class SunriseSunset(
    val updateTime: String,
    val sunrise: String,
    val sunset: String,
)

sealed interface SunriseSunsetFetchResult {
    data class Available(
        val sunriseSunset: SunriseSunset,
    ) : SunriseSunsetFetchResult

    data class Failure(
        val reason: SunriseSunsetFailureReason,
    ) : SunriseSunsetFetchResult
}

enum class SunriseSunsetFailureReason {
    Timeout,
    QuotaExceeded,
    Unauthorized,
    Unknown,
}
