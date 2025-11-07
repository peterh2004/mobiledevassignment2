package com.example.spotfinder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val address: String,
    val latitude: Double,
    val longitude: Double
)
