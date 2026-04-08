package io.github.yeweijiehust.weatherforecast.data.remote.mapper

import io.github.yeweijiehust.weatherforecast.data.remote.dto.CityDto
import io.github.yeweijiehust.weatherforecast.domain.model.City

fun CityDto.toDomain(): City {
    return City(
        id = id,
        name = name,
        adm1 = adm1,
        adm2 = adm2,
        country = country,
        lat = lat,
        lon = lon,
        timeZone = tz,
        isDefault = false,
    )
}
