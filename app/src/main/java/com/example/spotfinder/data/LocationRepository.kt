package com.example.spotfinder.data

import com.example.spotfinder.data.local.LocationDao
import com.example.spotfinder.data.local.LocationEntity
import kotlinx.coroutines.flow.Flow

class LocationRepository(private val locationDao: LocationDao) {

    suspend fun getAllLocations(): List<LocationEntity> = locationDao.getAllLocations()

    fun observeLocations(): Flow<List<LocationEntity>> = locationDao.observeLocations()

    suspend fun getLocationByAddress(address: String): LocationEntity? =
        locationDao.getLocationByAddress(address.trim())

    suspend fun addLocation(location: LocationEntity): Boolean =
        locationDao.insertLocation(location) != -1L

    suspend fun updateLocation(location: LocationEntity) {
        locationDao.updateLocation(location)
    }

    suspend fun deleteLocation(location: LocationEntity) {
        locationDao.deleteLocation(location)
    }
}
