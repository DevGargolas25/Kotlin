package com.example.brigadist.ui.brigadist

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brigadist.Orquestador
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.ui.map.components.RecenterButton
import com.example.brigadist.ui.sos.model.Emergency
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun BrigadistMapScreen(
    orquestador: Orquestador
) {
    val context = LocalContext.current
    val emergencyRepository = remember { EmergencyRepository(context) }
    val brigadistEmail = orquestador.getUserProfile().email
    
    // State for emergencies
    var emergencies by remember { mutableStateOf<List<Pair<String, Emergency>>>(emptyList()) }
    var brigadistLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasInProgressEmergency by remember { mutableStateOf(false) }
    var closestEmergency by remember { mutableStateOf<Pair<String, Emergency>?>(null) }
    
    // Get brigadist's current location - update periodically
    LaunchedEffect(Unit) {
        val fusedLocationClient: FusedLocationProviderClient = 
            LocationServices.getFusedLocationProviderClient(context)
        
        suspend fun updateLocation() {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        brigadistLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Handle permission denied
            }
        }
        
        updateLocation()
        
        while (true) {
            delay(5000)
            updateLocation()
        }
    }
    
    // Listen to emergencies
    DisposableEffect(Unit) {
        var currentHasInProgress = false
        
        // Check for "In progress" emergencies assigned to this brigadist
        val inProgressListener = emergencyRepository.listenToEmergenciesByStatus(
            listOf("In progress")
        ) { emergencyList ->
            val assignedInProgress = emergencyList.filter { (_, emergency) ->
                emergency.assignedBrigadistId == brigadistEmail &&
                emergency.latitude != 0.0 && emergency.longitude != 0.0
            }
            
            currentHasInProgress = assignedInProgress.isNotEmpty()
            hasInProgressEmergency = currentHasInProgress
            
            if (currentHasInProgress) {
                emergencies = assignedInProgress
                // Set the in-progress emergency as the closest (only one)
                closestEmergency = assignedInProgress.firstOrNull()
            }
        }
        
        // Listen to "Unattended" emergencies only
        val unattendedListener = emergencyRepository.listenToEmergenciesByStatus(
            listOf("Unattended")
        ) { emergencyList ->
            if (!hasInProgressEmergency) {
                val validEmergencies = emergencyList.filter { (_, emergency) ->
                    emergency.latitude != 0.0 && emergency.longitude != 0.0 &&
                    emergency.status == "Unattended"
                }
                
                emergencies = validEmergencies
                
                // Find closest emergency
                if (validEmergencies.isNotEmpty() && brigadistLocation != null) {
                    val closest = validEmergencies.minByOrNull { (_, emergency) ->
                        val emergencyLocation = LatLng(emergency.latitude, emergency.longitude)
                        calculateDistance(brigadistLocation!!, emergencyLocation)
                    }
                    closestEmergency = closest
                } else {
                    closestEmergency = null
                }
            }
        }
        
        onDispose {
            emergencyRepository.removeListener(inProgressListener)
            emergencyRepository.removeListener(unattendedListener)
        }
    }
    
    // Camera position - center on closest emergency or default
    val defaultLocation = LatLng(4.6018, -74.0661) // University default
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(closestEmergency?.let { 
                LatLng(it.second.latitude, it.second.longitude) 
            } ?: defaultLocation)
            .zoom(16f)
            .build()
    }
    
    // Update camera when closest emergency changes
    LaunchedEffect(closestEmergency) {
        closestEmergency?.let { (_, emergency) ->
            val target = LatLng(emergency.latitude, emergency.longitude)
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(target)
                        .zoom(16f)
                        .build()
                )
            )
        }
    }
    
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.NORMAL
                )
            ) {
                // Add markers for all emergencies
                emergencies.forEach { (key, emergency) ->
                    val isClosest = closestEmergency?.first == key
                    val emergencyLocation = LatLng(emergency.latitude, emergency.longitude)
                    
                    Marker(
                        state = MarkerState(position = emergencyLocation),
                        title = emergency.emerType,
                        snippet = "Location: ${emergency.location}",
                        icon = if (isClosest) {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        } else {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        }
                    )
                }
            }
            
            // Recenter button
            RecenterButton(
                onClick = {
                    closestEmergency?.let { (_, emergency) ->
                        val target = LatLng(emergency.latitude, emergency.longitude)
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                        .target(target)
                                        .zoom(16f)
                                        .build()
                                )
                            )
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
            
            // Show banner for closest emergency
            closestEmergency?.let { (emergencyKey, emergency) ->
                val distance = brigadistLocation?.let { location ->
                    val emergencyLocation = LatLng(emergency.latitude, emergency.longitude)
                    calculateDistance(location, emergencyLocation)
                }
                
                val etaMinutes = distance?.let { calculateEtaMinutes(it) }
                
                EmergencyMapBanner(
                    emergency = emergency,
                    emergencyKey = emergencyKey,
                    distance = distance,
                    etaMinutes = etaMinutes,
                    brigadistEmail = brigadistEmail,
                    emergencyRepository = emergencyRepository,
                    isInProgress = hasInProgressEmergency,
                    onNavigateClick = {
                        openWalkingDirections(context, emergency)
                    },
                    onViewOnMapClick = {
                        val target = LatLng(emergency.latitude, emergency.longitude)
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                        .target(target)
                                        .zoom(16f)
                                        .build()
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmergencyMapBanner(
    emergency: Emergency,
    emergencyKey: String,
    distance: Double?,
    etaMinutes: Int?,
    brigadistEmail: String,
    emergencyRepository: EmergencyRepository,
    isInProgress: Boolean,
    onNavigateClick: () -> Unit,
    onViewOnMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distanceText = distance?.let { formatDistance(it) } ?: "N/A"
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var isAttending by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (emergency.status == "In progress") "Emergency In Progress" else "Nearest Emergency",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = emergency.emerType,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                etaMinutes?.let {
                    Text(
                        text = "~$it min walking",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Show "Attend" button only for unattended emergencies
            if (!isInProgress && emergency.status == "Unattended") {
                Button(
                    onClick = { showConfirmationDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAttending,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isAttending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attending...")
                    } else {
                        Text("Attend Emergency")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Navigate",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Navigate")
                }
                
                OutlinedButton(
                    onClick = onViewOnMapClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "View on map",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View on map")
                }
            }
        }
    }
    
    // Confirmation dialog for attending emergency
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text("Attend Emergency")
            },
            text = {
                Text("Are you sure you want to attend this ${emergency.emerType.lowercase()} emergency? This will mark you as the assigned brigadist.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        isAttending = true
                        
                        // Update emergency status and assign brigadist
                        emergencyRepository.updateEmergencyStatus(
                            emergencyKey = emergencyKey,
                            newStatus = "In progress",
                            brigadistEmail = brigadistEmail,
                            onSuccess = {
                                isAttending = false
                                // Status will be updated automatically via Firebase listeners
                            },
                            onError = { error ->
                                isAttending = false
                                // Could show an error message here
                            }
                        )
                    },
                    enabled = !isAttending
                ) {
                    Text("Attend")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false },
                    enabled = !isAttending
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun openWalkingDirections(context: Context, emergency: Emergency) {
    val uri = Uri.parse("google.navigation:q=${emergency.latitude},${emergency.longitude}&mode=w")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")
    
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to web browser if Google Maps app is not available
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${emergency.latitude},${emergency.longitude}&travelmode=walking")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        context.startActivity(webIntent)
    }
}

private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
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

private fun formatDistance(meters: Double): String {
    return when {
        meters < 1000 -> "${meters.roundToInt()} m"
        else -> "${(meters / 1000).roundToInt()} km"
    }
}

private fun calculateEtaMinutes(distanceMeters: Double): Int {
    // Walking speed: ~5 km/h = 83.33 m/min
    val etaMinutes = (distanceMeters / 83.33).toInt()
    return maxOf(1, etaMinutes) // Minimum 1 minute
}

private fun Double.roundToInt(): Int = kotlin.math.round(this).toInt()


