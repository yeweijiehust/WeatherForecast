package io.github.yeweijiehust.weatherforecast.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_weather_cache")
data class CurrentWeatherEntity(
    @PrimaryKey
    @ColumnInfo(name = "location_id")
    val cityId: String,
    @ColumnInfo(name = "observed_at")
    val observationTime: String,
    val temperature: String,
    @ColumnInfo(name = "feels_like")
    val feelsLike: String,
    @ColumnInfo(name = "condition_text")
    val conditionText: String,
    @ColumnInfo(name = "condition_icon")
    val conditionIcon: String,
    val humidity: String,
    @ColumnInfo(name = "wind_direction")
    val windDirection: String,
    @ColumnInfo(name = "wind_scale")
    val windScale: String,
    @ColumnInfo(name = "wind_speed")
    val windSpeed: String,
    val precipitation: String,
    val pressure: String,
    val visibility: String,
    @ColumnInfo(name = "fetched_at")
    val fetchedAtEpochMillis: Long,
    val language: String,
    @ColumnInfo(name = "unit_system")
    val unitSystem: String,
)
