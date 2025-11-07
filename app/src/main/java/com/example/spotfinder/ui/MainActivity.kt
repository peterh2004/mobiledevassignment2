package com.example.spotfinder.ui

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.spotfinder.R
import com.example.spotfinder.data.LocationEntity
import com.example.spotfinder.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: SpotFinderViewModel by viewModels()
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
        initializeMap()
    }

    private fun setupListeners() {
        binding.searchButton.setOnClickListener { handleSearch() }
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handleSearch()
                true
            } else {
                false
            }
        }

        binding.addButton.setOnClickListener { handleAdd() }
        binding.updateButton.setOnClickListener { handleUpdate() }
        binding.deleteButton.setOnClickListener { handleDelete() }
    }

    private fun observeViewModel() {
        viewModel.locations.observe(this) { locations ->
            updateMapMarkers(locations)
        }
        viewModel.selectedLocation.observe(this) { location ->
            updateSelectedLocation(location)
        }
        viewModel.message.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.consumeMessage()
            }
        }
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map.apply {
            uiSettings.isZoomControlsEnabled = true
            moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_TORONTO_CENTER, DEFAULT_ZOOM))
        }
        viewModel.locations.value?.let { updateMapMarkers(it) }
        viewModel.selectedLocation.value?.let { updateSelectedLocation(it) }
    }

    private fun handleSearch() {
        val address = binding.searchInput.text?.toString().orEmpty()
        viewModel.search(address)
    }

    private fun handleAdd() {
        val address = binding.addressInput.text?.toString().orEmpty()
        val latitude = binding.latitudeInput.text?.toString()?.toDoubleOrNull()
        val longitude = binding.longitudeInput.text?.toString()?.toDoubleOrNull()
        if (!validateCoordinates(latitude, longitude)) return
        viewModel.addLocation(address, latitude!!, longitude!!)
    }

    private fun handleUpdate() {
        val address = binding.addressInput.text?.toString().orEmpty()
        val latitude = binding.latitudeInput.text?.toString()?.toDoubleOrNull()
        val longitude = binding.longitudeInput.text?.toString()?.toDoubleOrNull()
        if (!validateCoordinates(latitude, longitude)) return
        viewModel.updateLocation(address, latitude!!, longitude!!)
    }

    private fun handleDelete() {
        val address = binding.addressInput.text?.toString().orEmpty()
        viewModel.deleteLocation(address)
    }

    private fun validateCoordinates(latitude: Double?, longitude: Double?): Boolean {
        if (latitude == null || longitude == null) {
            Toast.makeText(this, "Please provide numeric latitude and longitude values.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            Toast.makeText(this, "Latitude or longitude values are out of range.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun updateMapMarkers(locations: List<LocationEntity>) {
        val map = googleMap ?: return
        map.clear()
        locations.forEach { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(location.address)
            )
        }
    }

    private fun updateSelectedLocation(location: LocationEntity?) {
        if (location == null) {
            binding.resultText.text = getString(R.string.result_placeholder)
            return
        }
        binding.resultText.text = getString(R.string.coordinates_format, location.latitude, location.longitude)
        binding.addressInput.setText(location.address)
        binding.searchInput.setText(location.address)
        binding.latitudeInput.setText(String.format(Locale.US, "%.6f", location.latitude))
        binding.longitudeInput.setText(String.format(Locale.US, "%.6f", location.longitude))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), FOCUSED_ZOOM))
    }

    companion object {
        private val DEFAULT_TORONTO_CENTER = LatLng(43.6532, -79.3832)
        private const val DEFAULT_ZOOM = 9.5f
        private const val FOCUSED_ZOOM = 14f
    }
}
