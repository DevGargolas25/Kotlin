package com.example.brigadist.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

/**
 * Utility functions for calculating and formatting distances between geographic coordinates.
 */
object DistanceUtils {
    /**
     * Calculates the distance between two geographic points using the Haversine formula.
     * 
     * @param point1 First geographic point
     * @param point2 Second geographic point
     * @return Distance in meters
     */
    fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371000.0 // Earth's radius in meters
        
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)
        
        val a = sin(deltaLatRad / 2) * sin(deltaLatRad / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLngRad / 2) * sin(deltaLngRad / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Formats a distance in meters to a human-readable string.
     * 
     * @param meters Distance in meters
     * @return Formatted string (e.g., "500 m" or "2.5 km")
     */
    fun formatDistance(meters: Double): String {
        return when {
            meters < 1000 -> "${meters.toInt()} m"
            else -> String.format("%.2f km", meters / 1000)
        }
    }
}
