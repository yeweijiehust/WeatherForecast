package io.github.yeweijiehust.weatherforecast.data.remote.mapper

import io.github.yeweijiehust.weatherforecast.data.remote.dto.MinutePrecipitationResponseDto
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationPoint
import io.github.yeweijiehust.weatherforecast.domain.model.MinutePrecipitationTimeline

fun MinutePrecipitationResponseDto.toDomainOrNull(): MinutePrecipitationTimeline? {
    if (minutely.isEmpty()) {
        return null
    }

    return MinutePrecipitationTimeline(
        updateTime = updateTime.orEmpty(),
        summary = summary.orEmpty(),
        points = minutely.map { point ->
            MinutePrecipitationPoint(
                forecastTime = point.forecastTime,
                precipitation = point.precipitation.orEmpty(),
                type = point.type.orEmpty(),
            )
        },
    )
}
