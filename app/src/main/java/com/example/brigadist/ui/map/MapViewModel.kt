package com.example.brigadist.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    
    // Default location: Universidad de los Andes, Bogotá
    private val defaultLocation = LatLng(4.6018, -74.0661)
    
    // Camera position state
    private val _cameraPosition = MutableStateFlow(
        CameraPosition.Builder()
            .target(defaultLocation)
            .zoom(15f)
            .build()
    )
    val cameraPosition: StateFlow<CameraPosition> = _cameraPosition.asStateFlow()
    
    // Map type state
    private val _mapType = MutableStateFlow(MapType.NORMAL)
    val mapType: StateFlow<MapType> = _mapType.asStateFlow()
    
    // User location state
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()
    
    // Permission state
    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()
    
    fun updateCameraPosition(position: CameraPosition) {
        _cameraPosition.value = position
    }
    
    fun setMapType(mapType: MapType) {
        _mapType.value = mapType
    }
    
    fun updateUserLocation(location: LatLng?) {
        _userLocation.value = location
    }
    
    fun setLocationPermission(granted: Boolean) {
        _hasLocationPermission.value = granted
    }
    
    fun recenterOnUser() {
        viewModelScope.launch {
            val location = _userLocation.value
            if (location != null) {
                val newPosition = CameraPosition.Builder()
                    .target(location)
                    .zoom(16f)
                    .build()
                _cameraPosition.value = newPosition
            }
        }
    }
    
    fun recenterOnDefault() {
        viewModelScope.launch {
            val newPosition = CameraPosition.Builder()
                .target(defaultLocation)
                .zoom(15f)
                .build()
            _cameraPosition.value = newPosition
        }
    }
}
