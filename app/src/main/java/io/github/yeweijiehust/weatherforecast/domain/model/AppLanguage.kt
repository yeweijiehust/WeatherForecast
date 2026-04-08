package io.github.yeweijiehust.weatherforecast.domain.model

enum class AppLanguage(
    val storageValue: String,
    val apiCode: String,
    val displayName: String,
) {
    English(
        storageValue = "en",
        apiCode = "en",
        displayName = "English",
    ),
    SimplifiedChinese(
        storageValue = "zh",
        apiCode = "zh",
        displayName = "Simplified Chinese",
    ),
}
