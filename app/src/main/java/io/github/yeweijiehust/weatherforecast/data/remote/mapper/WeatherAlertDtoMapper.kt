package io.github.yeweijiehust.weatherforecast.data.remote.mapper

import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherAlertDto
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherAlert

fun WeatherAlertDto.toDomain(): WeatherAlert {
    return WeatherAlert(
        id = id,
        sender = senderName.orEmpty(),
        publishTime = issuedTime.orEmpty(),
        title = headline.orIfBlank(title).orEmpty(),
        startTime = onsetTime.orIfBlank(effectiveTime).orEmpty(),
        endTime = expireTime.orEmpty(),
        status = status.orIfBlank(messageType?.code).orEmpty(),
        severity = severity.orEmpty(),
        severityColor = color?.code.orEmpty(),
        type = eventType?.code.orEmpty(),
        typeName = eventType?.name.orEmpty(),
        text = description.orIfBlank(instruction).orEmpty(),
    )
}

private fun String?.orIfBlank(alternative: String?): String? {
    return if (this.isNullOrBlank()) alternative else this
}
