package io.github.yeweijiehust.weatherforecast.data.local.model

data class SavedCityLocalModel(
    val locationId: String,
    val name: String,
    val adm1: String,
    val adm2: String,
    val country: String,
    val lat: String,
    val lon: String,
    val timeZone: String,
    val sortOrder: Int,
    val createdAtEpochMillis: Long,
)
