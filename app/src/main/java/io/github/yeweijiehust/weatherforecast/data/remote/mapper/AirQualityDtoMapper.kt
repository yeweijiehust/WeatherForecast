package io.github.yeweijiehust.weatherforecast.data.remote.mapper

import io.github.yeweijiehust.weatherforecast.data.remote.dto.AirQualityCurrentDto
import io.github.yeweijiehust.weatherforecast.domain.model.AirQuality

fun AirQualityCurrentDto.toDomain(): AirQuality {
    return AirQuality(
        publishTime = publishTime.orEmpty(),
        aqi = aqi.orFallback(),
        category = category.orFallback(),
        primary = primary.orFallback(),
        pm2p5 = pm2p5.orFallback(),
        pm10 = pm10.orFallback(),
        no2 = no2.orFallback(),
        so2 = so2.orFallback(),
        co = co.orFallback(),
        o3 = o3.orFallback(),
    )
}

private fun String?.orFallback(): String = if (this.isNullOrBlank()) "--" else this
