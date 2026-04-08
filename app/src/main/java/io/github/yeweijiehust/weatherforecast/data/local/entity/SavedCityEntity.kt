package io.github.yeweijiehust.weatherforecast.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_city")
data class SavedCityEntity(
    @PrimaryKey
    @ColumnInfo(name = "location_id")
    val locationId: String,
    val name: String,
    val adm1: String,
    val adm2: String,
    val country: String,
    val lat: String,
    val lon: String,
    @ColumnInfo(name = "time_zone")
    val timeZone: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,
)
