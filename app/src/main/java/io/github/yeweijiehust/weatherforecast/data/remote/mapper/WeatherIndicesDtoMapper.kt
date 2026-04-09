package io.github.yeweijiehust.weatherforecast.data.remote.mapper

import io.github.yeweijiehust.weatherforecast.data.remote.dto.WeatherIndicesResponseDto
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndex
import io.github.yeweijiehust.weatherforecast.domain.model.WeatherIndices

fun WeatherIndicesResponseDto.toDomain(): WeatherIndices {
    return WeatherIndices(
        updateTime = updateTime.orEmpty(),
        items = daily.map { item ->
            WeatherIndex(
                date = item.date.orEmpty(),
                type = item.type.orEmpty(),
                name = item.name.orEmpty(),
                level = item.level.orEmpty(),
                category = item.category.orEmpty(),
                text = item.text.orEmpty(),
            )
        }.sortedWith(
            compareBy(
                { weatherIndex -> prioritizedTypeOrder(weatherIndex.type) },
                { weatherIndex -> weatherIndex.type.toIntOrNull() ?: Int.MAX_VALUE },
            ),
        ),
    )
}

private fun prioritizedTypeOrder(type: String): Int {
    return when (type) {
        "5" -> 0 // UV Index
        "8" -> 1 // Comfort
        "1" -> 2 // Sport
        "6" -> 3 // Travel
        "3" -> 4 // Dressing
        "9" -> 5 // Flu
        "10" -> 6 // Air pollution
        else -> 100
    }
}
