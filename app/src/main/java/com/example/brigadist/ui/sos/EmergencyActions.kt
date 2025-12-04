package com.example.brigadist.ui.sos

import android.content.Context
import com.example.brigadist.Orquestador
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.ui.sos.components.EmergencyType
import com.example.brigadist.ui.sos.model.Emergency
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar

object EmergencyActions {
    // University coordinates
    private val UNIVERSITY_COORDINATES = LatLng(4.602813, -74.065071)
    // Distance threshold in meters (2 km = 2000 m)
    private const val DISTANCE_THRESHOLD_METERS = 2000.0

    fun createAndSaveEmergency(
        context: Context,
        emergencyType: EmergencyType,
        emergencyRepository: EmergencyRepository,
        orquestador: Orquestador,
        chatUsed: Boolean = false,
        skipDistanceCheck: Boolean = false,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onOffline: () -> Unit,
        onDistanceWarning: ((Double, () -> Unit) -> Unit)? = null
    ) {
        val userId = orquestador.getUserProfile().email

        val emerTypeString = when (emergencyType) {
            EmergencyType.FIRE -> "Fire"
            EmergencyType.EARTHQUAKE -> "Earthquake"
            EmergencyType.MEDICAL -> "Medical"
        }

        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val currentLat = location?.latitude ?: 0.0
                val currentLng = location?.longitude ?: 0.0
                val currentLocation = if (location != null) {
                    LatLng(currentLat, currentLng)
                } else {
                    null
                }

                val buildingName = if (currentLocation != null) {
                    getBuildingNameFromLocation(currentLocation)
                } else {
                    "SD"
                }

                // Check distance from university if location is available and check is not skipped
                if (currentLocation != null && !skipDistanceCheck) {
                    val distanceFromUniversity = calculateDistance(currentLocation, UNIVERSITY_COORDINATES)
                    if (distanceFromUniversity > DISTANCE_THRESHOLD_METERS) {
                        // Show distance warning
                        onDistanceWarning?.invoke(distanceFromUniversity) {
                            // User confirmed, proceed with emergency creation
                            createAndSaveEmergency(
                                context = context,
                                emergencyType = emergencyType,
                                emergencyRepository = emergencyRepository,
                                orquestador = orquestador,
                                chatUsed = chatUsed,
                                skipDistanceCheck = true,
                                onSuccess = onSuccess,
                                onError = onError,
                                onOffline = onOffline,
                                onDistanceWarning = null // Don't show warning again
                            )
                        }
                        return@addOnSuccessListener
                    }
                }

                val now = System.currentTimeMillis()
                val emergency = Emergency(
                    EmerResquestTime = now,  // Set to current time for proper tracking
                    assignedBrigadistId = "",
                    createdAt = now,
                    date_time = formatDateTime(),
                    emerType = emerTypeString,
                    emergencyID = 0,
                    location = buildingName,
                    latitude = currentLat,
                    longitude = currentLng,
                    secondsResponse = 5,
                    seconds_response = 5,
                    updatedAt = now,
                    userId = userId,
                    ChatUsed = chatUsed
                ).also { 
                    // Ensure ChatUsed field is explicitly set with capital C
                    it.ChatUsed = chatUsed 
                }

                emergencyRepository.createEmergency(
                    emergency = emergency,
                    onSuccess = onSuccess,
                    onError = onError,
                    onOffline = onOffline
                )
            }.addOnFailureListener {
                // Location unavailable, create emergency with default coordinates
                val now = System.currentTimeMillis()
                val emergency = Emergency(
                    EmerResquestTime = now,  // Set to current time for proper tracking
                    assignedBrigadistId = "",
                    createdAt = now,
                    date_time = formatDateTime(),
                    emerType = emerTypeString,
                    emergencyID = 0,
                    location = "SD",
                    latitude = 0.0,
                    longitude = 0.0,
                    secondsResponse = 5,
                    seconds_response = 5,
                    updatedAt = now,
                    userId = userId,
                    ChatUsed = chatUsed
                ).also { 
                    // Ensure ChatUsed field is explicitly set with capital C
                    it.ChatUsed = chatUsed 
                }

                emergencyRepository.createEmergency(
                    emergency = emergency,
                    onSuccess = onSuccess,
                    onError = onError,
                    onOffline = onOffline
                )
            }
        } catch (e: SecurityException) {
            // Location permission denied, create emergency with default coordinates
            val now = System.currentTimeMillis()
            val emergency = Emergency(
                EmerResquestTime = now,  // Set to current time for proper tracking
                assignedBrigadistId = "",
                createdAt = now,
                date_time = formatDateTime(),
                emerType = emerTypeString,
                emergencyID = 0,
                location = "SD",
                latitude = 0.0,
                longitude = 0.0,
                secondsResponse = 5,
                seconds_response = 5,
                updatedAt = now,
                userId = userId,
                ChatUsed = chatUsed
            ).also { 
                // Ensure ChatUsed field is explicitly set with capital C
                it.ChatUsed = chatUsed 
            }

            emergencyRepository.createEmergency(
                emergency = emergency,
                onSuccess = onSuccess,
                onError = onError,
                onOffline = onOffline
            )
        }
    }

    internal fun getBuildingNameFromLocation(location: LatLng): String {
        data class BuildingInfo(val name: String, val coordinates: LatLng)

        val buildings = listOf(
            BuildingInfo("LL", LatLng(4.602403, -74.065119)),
            BuildingInfo("ML", LatLng(4.602863, -74.065046)),
            BuildingInfo("CJ", LatLng(4.601025, -74.066549)),
            BuildingInfo("SD", LatLng(4.604219, -74.06588)),
            BuildingInfo("W", LatLng(4.602362, -74.065019)),
            BuildingInfo("AU", LatLng(4.602672, -74.06614)),
            BuildingInfo("C", LatLng(4.601188, -74.065074)),
            BuildingInfo("R", LatLng(4.601918, -74.064334)),
            BuildingInfo("RGD", LatLng(4.602478, -74.065988)),
            BuildingInfo("B", LatLng(4.601699, -74.065581))
        )

        var minDistance = Double.MAX_VALUE
        var closestBuilding = "SD"
        for (building in buildings) {
            val distance = calculateDistance(location, building.coordinates)
            if (distance < minDistance) {
                minDistance = distance
                closestBuilding = building.name
            }
        }
        return closestBuilding
    }

    internal fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val earthRadius = 6371000.0
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)
        val a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    internal fun formatDateTime(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
        val hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
        val minute = String.format("%02d", calendar.get(Calendar.MINUTE))
        val second = String.format("%02d", calendar.get(Calendar.SECOND))
        val micro = String.format("%06d", calendar.get(Calendar.MILLISECOND) * 1000)
        return "$year-$month-${'$'}dayT$hour:$minute:$second.$micro"
    }
}


