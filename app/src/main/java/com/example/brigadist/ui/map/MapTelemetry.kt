package com.example.brigadist.ui.map

import com.google.maps.android.compose.MapType

/**
 * Lightweight telemetry for Map screen interactions.
 * This is a placeholder implementation that can be replaced with actual analytics.
 */
object MapTelemetry {
    
    fun trackMapOpened(hasLocationPermission: Boolean, mapType: MapType) {
        // TODO: Replace with actual analytics implementation
        // Example: analytics.track("map_opened", mapOf(
        //     "hasLocationPermission" to hasLocationPermission,
        //     "mapType" to mapType.name.lowercase()
        // ))
    }
    
    fun trackRecenterTapped(success: Boolean) {
        // TODO: Replace with actual analytics implementation
        // Example: analytics.track("map_recenter_tapped", mapOf("success" to success))
    }
    
    fun trackMapTypeChanged(mapType: MapType) {
        // TODO: Replace with actual analytics implementation
        // Example: analytics.track("map_type_changed", mapOf("mapType" to mapType.name.lowercase()))
    }
    
    fun trackEvacuationNearestComputed(name: String, distanceBucket: String) {
        // TODO: Replace with actual analytics implementation
        // Example: analytics.track("evac_nearest_computed", mapOf(
        //     "name" to name,
        //     "distance_bucket" to distanceBucket
        // ))
    }
    
    fun trackEvacuationNavigateClicked(name: String) {
        // TODO: Replace with actual analytics implementation
        // Example: analytics.track("evac_navigate_clicked", mapOf("name" to name))
    }
}
