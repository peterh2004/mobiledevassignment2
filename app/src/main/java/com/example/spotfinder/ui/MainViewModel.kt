package com.example.spotfinder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spotfinder.data.LocationRepository
import com.example.spotfinder.data.local.LocationEntity
import com.example.spotfinder.data.local.SpotFinderDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocationRepository(SpotFinderDatabase.getInstance(application).locationDao())

    private val _allLocations = MutableLiveData<List<LocationEntity>>(emptyList())
    val allLocations: LiveData<List<LocationEntity>> = _allLocations

    private val _selectedLocation = MutableLiveData<LocationEntity?>()
    val selectedLocation: LiveData<LocationEntity?> = _selectedLocation

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    init {
        observeLocations()
    }

    fun loadLocations() {
        // Locations stream is already active; method kept for clarity.
    }

    fun searchLocation(address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val location = repository.getLocationByAddress(address)
            if (location != null) {
                _selectedLocation.postValue(location)
                _message.postValue("Showing ${location.address}")
            } else {
                _message.postValue("No stored location found for $address")
            }
        }
    }

    fun selectLocationByAddress(address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val location = repository.getLocationByAddress(address)
            location?.let {
                _selectedLocation.postValue(it)
            }
        }
    }

    fun addLocation(address: String, latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getLocationByAddress(address)
            if (existing != null) {
                _message.postValue("Location already exists for $address")
            } else {
                val inserted = repository.addLocation(
                    LocationEntity(address = address, latitude = latitude, longitude = longitude)
                )
                if (inserted) {
                    _message.postValue("Location added")
                } else {
                    _message.postValue("Unable to add location")
                }
            }
        }
    }

    fun updateLocation(address: String, latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getLocationByAddress(address)
            if (existing == null) {
                _message.postValue("No existing location for $address")
            } else {
                val updated = existing.copy(latitude = latitude, longitude = longitude)
                repository.updateLocation(updated)
                _selectedLocation.postValue(updated)
                _message.postValue("Location updated")
            }
        }
    }

    fun deleteLocation(address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getLocationByAddress(address)
            if (existing == null) {
                _message.postValue("No existing location for $address")
            } else {
                repository.deleteLocation(existing)
                _selectedLocation.postValue(null)
                _message.postValue("Location deleted")
            }
        }
    }

    private fun observeLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.observeLocations().collectLatest { locations ->
                _allLocations.postValue(locations)
            }
        }
    }

    fun clearMessage() {
        _message.postValue(null)
    }
}
