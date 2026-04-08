package io.github.yeweijiehust.weatherforecast.data.local.mapper

import io.github.yeweijiehust.weatherforecast.data.local.entity.SavedCityEntity
import io.github.yeweijiehust.weatherforecast.data.local.model.SavedCityLocalModel
import io.github.yeweijiehust.weatherforecast.domain.model.City

fun SavedCityEntity.toLocalModel(): SavedCityLocalModel {
    return SavedCityLocalModel(
        locationId = locationId,
        name = name,
        adm1 = adm1,
        adm2 = adm2,
        country = country,
        lat = lat,
        lon = lon,
        timeZone = timeZone,
        sortOrder = sortOrder,
        createdAtEpochMillis = createdAtEpochMillis,
    )
}

fun SavedCityLocalModel.toEntity(): SavedCityEntity {
    return SavedCityEntity(
        locationId = locationId,
        name = name,
        adm1 = adm1,
        adm2 = adm2,
        country = country,
        lat = lat,
        lon = lon,
        timeZone = timeZone,
        sortOrder = sortOrder,
        createdAtEpochMillis = createdAtEpochMillis,
    )
}

fun SavedCityLocalModel.toDomain(
    isDefault: Boolean,
): City {
    return City(
        id = locationId,
        name = name,
        adm1 = adm1,
        adm2 = adm2,
        country = country,
        lat = lat,
        lon = lon,
        timeZone = timeZone,
        isDefault = isDefault,
    )
}
