package com.example.spotfinder.data

import kotlinx.coroutines.flow.Flow

class LocationRepository(private val locationDao: LocationDao) {
    fun observeLocations(): Flow<List<LocationEntity>> = locationDao.observeAll()

    suspend fun findLocation(address: String): LocationEntity? =
        locationDao.findByAddress(address)

    suspend fun insertLocation(address: String, latitude: Double, longitude: Double): LocationEntity? {
        val entity = LocationEntity(address = address, latitude = latitude, longitude = longitude)
        val id = locationDao.insert(entity)
        return if (id != -1L) entity.copy(id = id.toInt()) else null
    }

    suspend fun updateLocation(address: String, latitude: Double, longitude: Double): LocationEntity? {
        val existing = locationDao.findByAddress(address) ?: return null
        val updated = existing.copy(latitude = latitude, longitude = longitude)
        locationDao.update(updated)
        return updated
    }

    suspend fun deleteLocation(address: String): Boolean {
        val existing = locationDao.findByAddress(address) ?: return false
        locationDao.delete(existing)
        return true
    }
}
