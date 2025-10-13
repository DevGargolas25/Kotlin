package com.example.brigadist.data

import com.google.android.gms.maps.model.LatLng

/**
 * Data class representing an evacuation point on the map.
 */
data class MapLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String = "Meeting point"
) {
    val latLng: LatLng
        get() = LatLng(latitude, longitude)
}

/**
 * Single source of truth for evacuation points.
 * Add more evacuation points here as needed.
 */
object MapLocations {
    
    val evacuationPoints = listOf(
        MapLocation(
            name = "ML Banderas",
            latitude = 4.603164,
            longitude = -74.065204,
            description = "Meeting point"
        ),
        MapLocation(
            name = "SD Cerca",
            latitude = 4.603966,
            longitude = -74.065778,
            description = "Meeting point"
        )
    )
    
    /**
     * Get all evacuation points as LatLng objects for map markers.
     */
    fun getAllEvacuationPoints(): List<MapLocation> = evacuationPoints
}
