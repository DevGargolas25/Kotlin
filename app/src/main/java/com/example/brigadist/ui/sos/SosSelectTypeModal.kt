package com.example.brigadist.ui.sos

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.brigadist.Orquestador
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.data.PendingEmergencyStore
import com.example.brigadist.ui.sos.components.EmergencyType
import com.example.brigadist.ui.sos.components.SosTypeRow
import com.example.brigadist.ui.sos.model.Emergency
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

@Composable
fun SosSelectTypeModal(
    onDismiss: () -> Unit,
    onTypeSelected: (EmergencyType) -> Unit,
    orquestador: Orquestador,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val emergencyRepository = remember { EmergencyRepository(context) }
    val pendingEmergencyStore = remember { PendingEmergencyStore(context) }
    var showOfflineMessage by remember { mutableStateOf<Boolean>(false) }
    // Track modal opened
    LaunchedEffect(Unit) {
        SosTelemetry.trackSosSelectTypeOpened()
    }
    
    Dialog(
        onDismissRequest = {
            SosTelemetry.trackSosSelectTypeClosed()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Scrim background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { 
                    SosTelemetry.trackSosSelectTypeClosed()
                    onDismiss() 
                },
            contentAlignment = Alignment.Center
        ) {
            // Modal content
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .clickable(enabled = false) { }, // Prevent clicks from bubbling to scrim
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    // Red header band with close button
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header content
                        SosSelectTypeHeader()
                        
                        // Close button positioned in top-right
                        IconButton(
                            onClick = {
                                SosTelemetry.trackSosSelectTypeClosed()
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                    
                    // Divider
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    
                    // White content area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Emergency type rows
                        SosTypeRow(
                            emergencyType = EmergencyType.FIRE,
                            subtitle = "Report fire emergency or smoke detection",
                            onClick = {
                                SosTelemetry.trackSosTypeSelected(EmergencyType.FIRE)
                                createAndSaveEmergency(
                                    context = context,
                                    emergencyType = EmergencyType.FIRE,
                                    emergencyRepository = emergencyRepository,
                                    orquestador = orquestador,
                                    pendingEmergencyStore = pendingEmergencyStore,
                                    onSuccess = {
                                        onTypeSelected(EmergencyType.FIRE)
                                        onDismiss()
                                    },
                                    onError = {
                                        // Still proceed with existing flow even if Firebase write fails
                                        onTypeSelected(EmergencyType.FIRE)
                                        onDismiss()
                                    },
                                    onOffline = {
                                        showOfflineMessage = true
                                        pendingEmergencyStore.setPendingEmergency(true)
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SosTypeRow(
                            emergencyType = EmergencyType.EARTHQUAKE,
                            subtitle = "Report seismic activity or structural damage",
                            onClick = {
                                SosTelemetry.trackSosTypeSelected(EmergencyType.EARTHQUAKE)
                                createAndSaveEmergency(
                                    context = context,
                                    emergencyType = EmergencyType.EARTHQUAKE,
                                    emergencyRepository = emergencyRepository,
                                    orquestador = orquestador,
                                    pendingEmergencyStore = pendingEmergencyStore,
                                    onSuccess = {
                                        onTypeSelected(EmergencyType.EARTHQUAKE)
                                        onDismiss()
                                    },
                                    onError = {
                                        // Still proceed with existing flow even if Firebase write fails
                                        onTypeSelected(EmergencyType.EARTHQUAKE)
                                        onDismiss()
                                    },
                                    onOffline = {
                                        showOfflineMessage = true
                                        pendingEmergencyStore.setPendingEmergency(true)
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SosTypeRow(
                            emergencyType = EmergencyType.MEDICAL,
                            subtitle = "Report medical emergency or injury",
                            onClick = {
                                SosTelemetry.trackSosTypeSelected(EmergencyType.MEDICAL)
                                createAndSaveEmergency(
                                    context = context,
                                    emergencyType = EmergencyType.MEDICAL,
                                    emergencyRepository = emergencyRepository,
                                    orquestador = orquestador,
                                    pendingEmergencyStore = pendingEmergencyStore,
                                    onSuccess = {
                                        onTypeSelected(EmergencyType.MEDICAL)
                                        onDismiss()
                                    },
                                    onError = {
                                        // Still proceed with existing flow even if Firebase write fails
                                        onTypeSelected(EmergencyType.MEDICAL)
                                        onDismiss()
                                    },
                                    onOffline = {
                                        showOfflineMessage = true
                                        pendingEmergencyStore.setPendingEmergency(true)
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Footer note
                        SosSelectTypeFooterNote()
                    }
                }
            }
        }
    }
    
    // Show offline message modal if needed
    if (showOfflineMessage) {
        SosOfflineMessageModal(
            onDismiss = { showOfflineMessage = false }
        )
    }
}

@Composable
private fun SosSelectTypeHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Alert icon in circular chip
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    MaterialTheme.colorScheme.onError.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Emergency Alert",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select Emergency Type",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onError,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose the type of emergency to report",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onError.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SosSelectTypeFooterNote(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Emergency personnel will be notified immediately upon selection.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Maps location coordinates to building name for Universidad de los Andes.
 * Returns the closest building based on distance calculation.
 */
internal fun getBuildingNameFromLocation(location: LatLng): String {
    data class BuildingInfo(val name: String, val coordinates: LatLng)
    
    val buildings = listOf(
        BuildingInfo("LL", LatLng(4.602403, -74.065119)),
        BuildingInfo("ML", LatLng( 4.602863, -74.065046)),
        BuildingInfo("CJ", LatLng(4.601025,-74.066549)),
        BuildingInfo("SD", LatLng(4.604219,-74.06588)),
        BuildingInfo("W", LatLng(4.602362,-74.065019)),
        BuildingInfo("AU", LatLng(4.602672, -74.06614)),
        BuildingInfo("C", LatLng(4.601188,-74.065074)),
        BuildingInfo("R", LatLng(4.601918, -74.064334)),
        BuildingInfo("RGD", LatLng(4.602478, -74.065988)),
        BuildingInfo("B", LatLng(4.601699, -74.065581)),


    )
    
    // Find the closest building using distance calculation
    var minDistance = Double.MAX_VALUE
    var closestBuilding = "SD" // Default fallback
    
    for (building in buildings) {
        val distance = calculateDistance(location, building.coordinates)
        if (distance < minDistance) {
            minDistance = distance
            closestBuilding = building.name
        }
    }
    
    return closestBuilding
}

/**
 * Calculates the distance between two LatLng points using the Haversine formula.
 * Returns distance in meters.
 */
internal fun calculateDistance(point1: LatLng, point2: LatLng): Double {
    val earthRadius = 6371000.0 // Earth's radius in meters
    
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

/**
 * Formats current date/time to match Firebase format: "2025-10-13T16:38:37.770010"
 */
internal fun formatDateTime(): String {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
    val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
    val hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
    val minute = String.format("%02d", calendar.get(Calendar.MINUTE))
    val second = String.format("%02d", calendar.get(Calendar.SECOND))
    // Format milliseconds to 6 digits (microseconds)
    val millisecond = String.format("%06d", calendar.get(Calendar.MILLISECOND) * 1000)
    
    return "$year-$month-${day}T$hour:$minute:$second.$millisecond"
}

/**
 * Creates and saves an emergency to Firebase when user selects an emergency type.
 */
internal fun createAndSaveEmergency(
    context: Context,
    emergencyType: EmergencyType,
    emergencyRepository: EmergencyRepository,
    orquestador: Orquestador,
    pendingEmergencyStore: PendingEmergencyStore,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onOffline: () -> Unit
) {
    // Get user ID from profile
    val userId = orquestador.getUserProfile().studentId.ifEmpty {
        orquestador.firebaseUserProfile?.studentId ?: ""
    }

    // Map EmergencyType enum to string
    val emerTypeString = when (emergencyType) {
        EmergencyType.FIRE -> "Fire"
        EmergencyType.EARTHQUAKE -> "Earthquake"
        EmergencyType.MEDICAL -> "Medical"
    }

    // Get current location and building name
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val buildingName = if (location != null) {
                getBuildingNameFromLocation(LatLng(location.latitude, location.longitude))
            } else {
                // Fallback to default if location not available
                "SD"
            }

            // Create emergency object
            val currentTime = System.currentTimeMillis()
            val emergency = Emergency(
                EmerResquestTime = 0,
                assignedBrigadistId = "",
                createdAt = currentTime,
                date_time = formatDateTime(),
                emerType = emerTypeString,
                emergencyID = 0, // Will be generated by repository
                location = buildingName,
                secondsResponse = 5,
                seconds_response = 5,
                updatedAt = currentTime,
                userId = userId
            )

            // Save to Firebase
            emergencyRepository.createEmergency(
                emergency = emergency,
                onSuccess = onSuccess,
                onError = onError,
                onOffline = onOffline
            )
        }.addOnFailureListener {
            // If location retrieval fails, use default building name
            val currentTime = System.currentTimeMillis()
            val emergency = Emergency(
                EmerResquestTime = 0,
                assignedBrigadistId = "",
                createdAt = currentTime,
                date_time = formatDateTime(),
                emerType = emerTypeString,
                emergencyID = 0,
                location = "SD", // Default building name
                secondsResponse = 5,
                seconds_response = 5,
                updatedAt = currentTime,
                userId = userId
            )

            emergencyRepository.createEmergency(
                emergency = emergency,
                onSuccess = onSuccess,
                onError = onError,
                onOffline = onOffline
            )
        }
    } catch (e: SecurityException) {
        // Permission denied, use default location
        val currentTime = System.currentTimeMillis()
        val emergency = Emergency(
            EmerResquestTime = 0,
            assignedBrigadistId = "",
            createdAt = currentTime,
            date_time = formatDateTime(),
            emerType = emerTypeString,
            emergencyID = 0,
            location = "SD",
            secondsResponse = 5,
            seconds_response = 5,
            updatedAt = currentTime,
            userId = userId
        )

        emergencyRepository.createEmergency(
            emergency = emergency,
            onSuccess = onSuccess,
            onError = onError,
            onOffline = onOffline
        )
    }
}
