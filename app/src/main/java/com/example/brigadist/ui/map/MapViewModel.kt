package com.example.brigadist.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapType
import com.example.brigadist.data.MapLocation
import com.example.brigadist.data.MapLocations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.*

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
    
    // Nearest evacuation point state
    private val _nearestLocation = MutableStateFlow<MapLocation?>(null)
    val nearestLocation: StateFlow<MapLocation?> = _nearestLocation.asStateFlow()
    
    private val _distanceMeters = MutableStateFlow<Double?>(null)
    val distanceMeters: StateFlow<Double?> = _distanceMeters.asStateFlow()
    
    private val _etaMinutes = MutableStateFlow<Int?>(null)
    val etaMinutes: StateFlow<Int?> = _etaMinutes.asStateFlow()
    
    // Location update tracking
    private var lastLocationUpdateTime = 0L
    private var lastKnownLocation: LatLng? = null
    private var locationProviderClient: FusedLocationProviderClient? = null
    
    // Constants for location updates
    private companion object {
        const val MIN_DISTANCE_CHANGE_METERS = 25.0
        const val MIN_TIME_BETWEEN_UPDATES_MS = 30000L // 30 seconds
        const val WALKING_SPEED_MPS = 1.4 // 5 km/h = 1.4 m/s
    }
    
    fun initializeLocationClient(context: Context) {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }
    
    fun updateCameraPosition(position: CameraPosition) {
        _cameraPosition.value = position
    }
    
    fun setMapType(mapType: MapType) {
        _mapType.value = mapType
    }
    
    fun updateUserLocation(location: LatLng?) {
        _userLocation.value = location
        
        // Check if we should recompute nearest location
        if (location != null && shouldUpdateNearestLocation(location)) {
            computeNearestEvacuationPoint(location)
            lastKnownLocation = location
            lastLocationUpdateTime = System.currentTimeMillis()
        }
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
    
    fun requestLocationUpdate() {
        locationProviderClient?.let { client ->
            viewModelScope.launch {
                try {
                    // First try to get last known location
                    client.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val latLng = LatLng(it.latitude, it.longitude)
                            updateUserLocation(latLng)
                        }
                    }
                    
                    // Then request fresh location updates
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        10000L // 10 seconds
                    ).apply {
                        setMinUpdateIntervalMillis(5000L) // 5 seconds minimum
                        setMaxUpdateDelayMillis(15000L) // 15 seconds maximum
                    }.build()
                    
                    client.requestLocationUpdates(
                        locationRequest,
                        { location ->
                            val latLng = LatLng(location.latitude, location.longitude)
                            updateUserLocation(latLng)
                        },
                        null
                    )
                } catch (e: SecurityException) {
                    // Permission not granted
                }
            }
        }
    }
    
    private fun shouldUpdateNearestLocation(newLocation: LatLng): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Always update if this is the first location
        if (lastKnownLocation == null) return true
        
        // Update if enough time has passed
        if (currentTime - lastLocationUpdateTime >= MIN_TIME_BETWEEN_UPDATES_MS) return true
        
        // Update if moved enough distance
        val distance = calculateDistance(lastKnownLocation!!, newLocation)
        return distance >= MIN_DISTANCE_CHANGE_METERS
    }
    
    private fun computeNearestEvacuationPoint(userLocation: LatLng) {
        viewModelScope.launch {
            val evacuationPoints = MapLocations.getAllEvacuationPoints()
            var nearestPoint: MapLocation? = null
            var minDistance = Double.MAX_VALUE
            
            evacuationPoints.forEach { point ->
                val distance = calculateDistance(userLocation, point.latLng)
                if (distance < minDistance) {
                    minDistance = distance
                    nearestPoint = point
                }
            }
            
            nearestPoint?.let { point ->
                _nearestLocation.value = point
                _distanceMeters.value = minDistance
                _etaMinutes.value = calculateEtaMinutes(minDistance)
                
                // Track telemetry
                val distanceBucket = when {
                    minDistance < 100 -> "<100m"
                    minDistance < 500 -> "100–500m"
                    else -> ">500m"
                }
                MapTelemetry.trackEvacuationNearestComputed(point.name, distanceBucket)
            }
        }
    }
    
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371000.0 // Earth's radius in meters
        
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)
        
        val a = sin(deltaLatRad / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLngRad / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    private fun calculateEtaMinutes(distanceMeters: Double): Int {
        val etaSeconds = distanceMeters / WALKING_SPEED_MPS
        val etaMinutes = (etaSeconds / 60).roundToInt()
        return maxOf(1, etaMinutes) // Minimum 1 minute
    }
    
    fun getAllEvacuationPoints(): List<MapLocation> {
        return MapLocations.getAllEvacuationPoints()
    }
    
    override fun onCleared() {
        super.onCleared()
        // Stop location updates when ViewModel is cleared
        locationProviderClient?.removeLocationUpdates { /* callback */ }
    }
}
