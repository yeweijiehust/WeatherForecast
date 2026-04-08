package io.github.yeweijiehust.weatherforecast.domain.model

enum class AppLanguage(
    val storageValue: String,
    val apiCode: String,
) {
    English(
        storageValue = "en",
        apiCode = "en",
    ),
    SimplifiedChinese(
        storageValue = "zh",
        apiCode = "zh",
    ),
}
