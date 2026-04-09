package io.github.yeweijiehust.weatherforecast.domain.model

data class WeatherIndices(
    val updateTime: String,
    val items: List<WeatherIndex>,
)

data class WeatherIndex(
    val date: String,
    val type: String,
    val name: String,
    val level: String,
    val category: String,
    val text: String,
)

sealed interface WeatherIndicesFetchResult {
    data class Available(
        val weatherIndices: WeatherIndices,
    ) : WeatherIndicesFetchResult

    data object Empty : WeatherIndicesFetchResult

    data class Failure(
        val reason: WeatherIndicesFailureReason,
    ) : WeatherIndicesFetchResult
}

enum class WeatherIndicesFailureReason {
    Timeout,
    QuotaExceeded,
    Unauthorized,
    Unknown,
}
