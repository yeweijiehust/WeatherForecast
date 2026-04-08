package io.github.yeweijiehust.weatherforecast.domain.model

data class City(
    val id: String,
    val name: String,
    val adm1: String,
    val adm2: String,
    val country: String,
    val lat: String,
    val lon: String,
    val timeZone: String,
    val isDefault: Boolean = false,
)
