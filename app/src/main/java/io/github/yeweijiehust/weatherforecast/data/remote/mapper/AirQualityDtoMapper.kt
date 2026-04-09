package io.github.yeweijiehust.weatherforecast.data.remote.mapper

import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityResponseDto
import io.github.yeweijiehust.weatherforecast.domain.model.AirQuality
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

fun AirQualityResponseDto.toDomainOrNull(): AirQuality? {
    val firstIndex = indexes.firstOrNull() ?: return null
    val pollutantByCode = pollutants.associateBy { pollutant ->
        pollutant.code.orEmpty().lowercase()
    }

    return AirQuality(
        publishTime = "",
        aqi = firstIndex.aqiDisplay.orIfBlank(firstIndex.aqi.asStringOrNull()).orFallback(),
        category = firstIndex.category.orFallback(),
        primary = firstIndex.primaryPollutant?.name
            .orIfBlank(firstIndex.primaryPollutant?.code)
            .orFallback(),
        pm2p5 = pollutantByCode["pm2p5"]?.concentration?.value.asStringOrNull().orFallback(),
        pm10 = pollutantByCode["pm10"]?.concentration?.value.asStringOrNull().orFallback(),
        no2 = pollutantByCode["no2"]?.concentration?.value.asStringOrNull().orFallback(),
        so2 = pollutantByCode["so2"]?.concentration?.value.asStringOrNull().orFallback(),
        co = pollutantByCode["co"]?.concentration?.value.asStringOrNull().orFallback(),
        o3 = pollutantByCode["o3"]?.concentration?.value.asStringOrNull().orFallback(),
    )
}

private fun String?.orIfBlank(alternative: String?): String? {
    return if (this.isNullOrBlank()) alternative else this
}

private fun JsonElement?.asStringOrNull(): String? {
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.contentOrNull
}

private fun String?.orFallback(): String = if (this.isNullOrBlank()) "--" else this
