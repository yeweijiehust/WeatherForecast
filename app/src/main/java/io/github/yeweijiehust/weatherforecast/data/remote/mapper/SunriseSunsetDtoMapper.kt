package io.github.yeweijiehust.weatherforecast.data.remote.mapper

import io.github.yeweijiehust.weatherforecast.data.remote.dto.SunriseSunsetResponseDto
import io.github.yeweijiehust.weatherforecast.domain.model.SunriseSunset

fun SunriseSunsetResponseDto.toDomain(): SunriseSunset {
    return SunriseSunset(
        updateTime = updateTime.orEmpty(),
        sunrise = sunrise.orEmpty(),
        sunset = sunset.orEmpty(),
    )
}
