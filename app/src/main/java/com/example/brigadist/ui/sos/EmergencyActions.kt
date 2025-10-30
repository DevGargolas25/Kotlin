package com.example.brigadist.ui.sos

import android.content.Context
import com.example.brigadist.Orquestador
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.data.PendingEmergencyStore
import com.example.brigadist.ui.sos.components.EmergencyType
import com.example.brigadist.ui.sos.model.Emergency
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar

object EmergencyActions {

    fun createAndSaveEmergency(
        context: Context,
        emergencyType: EmergencyType,
        emergencyRepository: EmergencyRepository,
        orquestador: Orquestador,
        pendingEmergencyStore: PendingEmergencyStore? = null,
        chatUsed: Boolean = false,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onOffline: () -> Unit
    ) {
        val userId = orquestador.getUserProfile().studentId.ifEmpty {
            orquestador.firebaseUserProfile?.studentId ?: ""
        }

        val emerTypeString = when (emergencyType) {
            EmergencyType.FIRE -> "Fire"
            EmergencyType.EARTHQUAKE -> "Earthquake"
            EmergencyType.MEDICAL -> "Medical"
        }

        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val buildingName = if (location != null) {
                    getBuildingNameFromLocation(LatLng(location.latitude, location.longitude))
                } else {
                    "SD"
                }

                val now = System.currentTimeMillis()
                val emergency = Emergency(
                    EmerResquestTime = 0,
                    assignedBrigadistId = "",
                    createdAt = now,
                    date_time = formatDateTime(),
                    emerType = emerTypeString,
                    emergencyID = 0,
                    location = buildingName,
                    secondsResponse = 5,
                    seconds_response = 5,
                    updatedAt = now,
                    userId = userId,
                    ChatUsed = chatUsed
                )

                emergencyRepository.createEmergency(
                    emergency = emergency,
                    onSuccess = onSuccess,
                    onError = onError,
                    onOffline = {
                        pendingEmergencyStore?.setPendingEmergency(true)
                        onOffline()
                    }
                )
            }.addOnFailureListener {
                val now = System.currentTimeMillis()
                val emergency = Emergency(
                    EmerResquestTime = 0,
                    assignedBrigadistId = "",
                    createdAt = now,
                    date_time = formatDateTime(),
                    emerType = emerTypeString,
                    emergencyID = 0,
                    location = "SD",
                    secondsResponse = 5,
                    seconds_response = 5,
                    updatedAt = now,
                    userId = userId,
                    ChatUsed = chatUsed
                )

                emergencyRepository.createEmergency(
                    emergency = emergency,
                    onSuccess = onSuccess,
                    onError = onError,
                    onOffline = {
                        pendingEmergencyStore?.setPendingEmergency(true)
                        onOffline()
                    }
                )
            }
        } catch (e: SecurityException) {
            val now = System.currentTimeMillis()
            val emergency = Emergency(
                EmerResquestTime = 0,
                assignedBrigadistId = "",
                createdAt = now,
                date_time = formatDateTime(),
                emerType = emerTypeString,
                emergencyID = 0,
                location = "SD",
                secondsResponse = 5,
                seconds_response = 5,
                updatedAt = now,
                userId = userId,
                ChatUsed = chatUsed
            )

            emergencyRepository.createEmergency(
                emergency = emergency,
                onSuccess = onSuccess,
                onError = onError,
                onOffline = {
                    pendingEmergencyStore?.setPendingEmergency(true)
                    onOffline()
                }
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


