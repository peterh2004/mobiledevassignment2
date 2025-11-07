package com.example.spotfinder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.spotfinder.data.AppDatabase
import com.example.spotfinder.data.LocationEntity
import com.example.spotfinder.data.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpotFinderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LocationRepository(AppDatabase.getDatabase(application).locationDao())

    val locations: LiveData<List<LocationEntity>> = repository.observeLocations().asLiveData()

    private val _selectedLocation = MutableLiveData<LocationEntity?>()
    val selectedLocation: LiveData<LocationEntity?> = _selectedLocation

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun search(addressInput: String) {
        val address = addressInput.trim()
        if (address.isEmpty()) {
            _message.value = "Please enter an address to search."
            _selectedLocation.value = null
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.findLocation(address)
            if (result != null) {
                _selectedLocation.postValue(result)
            } else {
                _selectedLocation.postValue(null)
                _message.postValue("No location found for that address.")
            }
        }
    }

    fun addLocation(addressInput: String, latitude: Double, longitude: Double) {
        val address = addressInput.trim()
        if (address.isEmpty()) {
            _message.value = "Address is required to add a location."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val inserted = repository.insertLocation(address, latitude, longitude)
            if (inserted != null) {
                _selectedLocation.postValue(inserted)
                _message.postValue("Location added successfully.")
            } else {
                _message.postValue("Address already exists. Try updating instead.")
            }
        }
    }

    fun updateLocation(addressInput: String, latitude: Double, longitude: Double) {
        val address = addressInput.trim()
        if (address.isEmpty()) {
            _message.value = "Address is required to update a location."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val updated = repository.updateLocation(address, latitude, longitude)
            if (updated != null) {
                _selectedLocation.postValue(updated)
                _message.postValue("Location updated successfully.")
            } else {
                _message.postValue("No saved location with that address to update.")
            }
        }
    }

    fun deleteLocation(addressInput: String) {
        val address = addressInput.trim()
        if (address.isEmpty()) {
            _message.value = "Address is required to delete a location."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val deleted = repository.deleteLocation(address)
            if (deleted) {
                _selectedLocation.postValue(null)
                _message.postValue("Location deleted successfully.")
            } else {
                _message.postValue("No saved location with that address to delete.")
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
