package com.example.spotfinder

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.spotfinder.data.local.LocationEntity
import com.example.spotfinder.databinding.ActivityMainBinding
import com.example.spotfinder.ui.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var googleMap: GoogleMap? = null
    private var initialCameraSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupListeners()
        observeViewModel()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map.apply {
            uiSettings.isZoomControlsEnabled = true
            setOnMarkerClickListener { marker ->
                marker.showInfoWindow()
                marker.title?.let { viewModel.selectLocationByAddress(it) }
                true
            }
        }
        viewModel.loadLocations()
    }

    private fun setupListeners() {
        binding.searchButton.setOnClickListener {
            val address = binding.addressInput.text.toString().trim()
            if (address.isEmpty()) {
                showToast(getString(R.string.validation_address))
            } else {
                viewModel.searchLocation(address)
            }
        }

        binding.addButton.setOnClickListener {
            handleCoordinateAction(ActionType.ADD)
        }

        binding.updateButton.setOnClickListener {
            handleCoordinateAction(ActionType.UPDATE)
        }

        binding.deleteButton.setOnClickListener {
            val address = binding.addressInput.text.toString().trim()
            if (address.isEmpty()) {
                showToast(getString(R.string.validation_address))
            } else {
                viewModel.deleteLocation(address)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.allLocations.observe(this) { locations ->
            updateMarkers(locations, viewModel.selectedLocation.value?.id)
        }

        viewModel.selectedLocation.observe(this) { location ->
            if (location != null) {
                updateMarkers(viewModel.allLocations.value.orEmpty(), location.id)
            }
        }

        viewModel.message.observe(this) { message ->
            if (!message.isNullOrBlank()) {
                showToast(message)
                viewModel.clearMessage()
            }
        }
    }

    private fun handleCoordinateAction(actionType: ActionType) {
        val address = binding.addressInput.text.toString().trim()
        val latText = binding.latitudeInput.text.toString().trim()
        val lngText = binding.longitudeInput.text.toString().trim()

        if (address.isEmpty()) {
            showToast(getString(R.string.validation_address))
            return
        }

        val latitude = parseLatitude(latText) ?: return
        val longitude = parseLongitude(lngText) ?: return

        when (actionType) {
            ActionType.ADD -> viewModel.addLocation(address, latitude, longitude)
            ActionType.UPDATE -> viewModel.updateLocation(address, latitude, longitude)
        }
    }

    private fun parseLatitude(value: String): Double? {
        val latitude = value.toDoubleOrNull()
        return if (latitude == null || latitude !in -90.0..90.0) {
            showToast(getString(R.string.validation_latitude))
            null
        } else {
            latitude
        }
    }

    private fun parseLongitude(value: String): Double? {
        val longitude = value.toDoubleOrNull()
        return if (longitude == null || longitude !in -180.0..180.0) {
            showToast(getString(R.string.validation_longitude))
            null
        } else {
            longitude
        }
    }

    private fun updateMarkers(locations: List<LocationEntity>, highlightId: Int?) {
        val map = googleMap ?: return
        map.clear()
        var highlightedMarker: Marker? = null
        locations.forEach { location ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title(location.address)
            )
            if (location.id == highlightId) {
                highlightedMarker = marker
            }
        }

        when {
            highlightedMarker != null -> {
                highlightedMarker?.showInfoWindow()
                highlightedMarker?.position?.let { latLng ->
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                }
                initialCameraSet = true
            }

            !initialCameraSet && locations.isNotEmpty() -> {
                val first = locations.first()
                val latLng = LatLng(first.latitude, first.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9f))
                initialCameraSet = true
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private enum class ActionType { ADD, UPDATE }
}
