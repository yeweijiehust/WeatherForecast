package io.github.yeweijiehust.weatherforecast.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "hourly_forecast_cache",
    primaryKeys = ["location_id", "forecast_time", "language", "unit_system"],
)
data class HourlyForecastEntity(
    @ColumnInfo(name = "location_id")
    val cityId: String,
    @ColumnInfo(name = "forecast_time")
    val forecastTime: String,
    val temperature: String,
    @ColumnInfo(name = "condition_text")
    val conditionText: String,
    @ColumnInfo(name = "condition_icon")
    val conditionIcon: String,
    @ColumnInfo(name = "precipitation_probability")
    val precipitationProbability: String,
    val precipitation: String,
    @ColumnInfo(name = "wind_direction")
    val windDirection: String,
    @ColumnInfo(name = "wind_scale")
    val windScale: String,
    @ColumnInfo(name = "wind_speed")
    val windSpeed: String,
    @ColumnInfo(name = "fetched_at")
    val fetchedAtEpochMillis: Long,
    val language: String,
    @ColumnInfo(name = "unit_system")
    val unitSystem: String,
)
