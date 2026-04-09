package io.github.yeweijiehust.weatherforecast.domain.model

data class MinutePrecipitationTimeline(
    val updateTime: String,
    val summary: String,
    val points: List<MinutePrecipitationPoint>,
)

data class MinutePrecipitationPoint(
    val forecastTime: String,
    val precipitation: String,
    val type: String,
)

sealed interface MinutePrecipitationFetchResult {
    data class Available(
        val timeline: MinutePrecipitationTimeline,
    ) : MinutePrecipitationFetchResult

    data object UnsupportedRegion : MinutePrecipitationFetchResult

    data class Failure(
        val reason: MinutePrecipitationFailureReason,
    ) : MinutePrecipitationFetchResult
}

enum class MinutePrecipitationFailureReason {
    Timeout,
    QuotaExceeded,
    Unauthorized,
    Unknown,
}
