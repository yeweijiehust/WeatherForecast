package io.github.yeweijiehust.weatherforecast.data.remote.mapper

import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertDto
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlert

fun WeatherAlertDto.toDomain(): WeatherAlert {
    return WeatherAlert(
        id = id,
        sender = sender,
        publishTime = publishTime,
        title = title,
        startTime = startTime,
        endTime = endTime,
        status = status,
        severity = severity,
        severityColor = severityColor,
        type = type,
        typeName = typeName,
        text = text,
    )
}
