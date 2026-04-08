package io.github.yeweijiehust.weatherforecast.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "daily_forecast_cache",
    primaryKeys = ["location_id", "forecast_date", "language", "unit_system"],
)
data class DailyForecastEntity(
    @ColumnInfo(name = "location_id")
    val cityId: String,
    @ColumnInfo(name = "forecast_date")
    val forecastDate: String,
    @ColumnInfo(name = "temp_max")
    val tempMax: String,
    @ColumnInfo(name = "temp_min")
    val tempMin: String,
    @ColumnInfo(name = "condition_text_day")
    val conditionTextDay: String,
    @ColumnInfo(name = "condition_icon_day")
    val conditionIconDay: String,
    @ColumnInfo(name = "precipitation_probability")
    val precipitationProbability: String,
    val precipitation: String,
    @ColumnInfo(name = "wind_direction_day")
    val windDirectionDay: String,
    @ColumnInfo(name = "wind_scale_day")
    val windScaleDay: String,
    @ColumnInfo(name = "wind_speed_day")
    val windSpeedDay: String,
    @ColumnInfo(name = "fetched_at")
    val fetchedAtEpochMillis: Long,
    val language: String,
    @ColumnInfo(name = "unit_system")
    val unitSystem: String,
)
