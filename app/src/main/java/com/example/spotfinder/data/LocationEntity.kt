package com.example.spotfinder.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "locations",
    indices = [Index(value = ["address"], unique = true)]
)
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val address: String,
    val latitude: Double,
    val longitude: Double
)
