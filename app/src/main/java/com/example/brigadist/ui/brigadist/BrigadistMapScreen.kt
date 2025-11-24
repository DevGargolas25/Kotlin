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
import com.example.brigadist.ui.sos.model.Emergency
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
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
    
    Box(modifier = Modifier.fillMaxSize()) {
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
        
        // Show banner for closest emergency
        closestEmergency?.let { (key, emergency) ->
            val distance = brigadistLocation?.let { location ->
                val emergencyLocation = LatLng(emergency.latitude, emergency.longitude)
                calculateDistance(location, emergencyLocation)
            }
            
            EmergencyMapBanner(
                emergency = emergency,
                distance = distance,
                onNavigateClick = {
                    openWalkingDirections(context, emergency)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun EmergencyMapBanner(
    emergency: Emergency,
    distance: Double?,
    onNavigateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emergencyTypeText = when (emergency.emerType.lowercase()) {
        "fire" -> "🔥 Fire"
        "medical" -> "🏥 Medical"
        "earthquake" -> "🌍 Earthquake"
        else -> emergency.emerType
    }
    
    val distanceText = distance?.let { formatDistance(it) } ?: "N/A"
    
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
                text = emergencyTypeText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Location: ${emergency.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onNavigateClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Navigate",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Navigate")
            }
        }
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
        meters < 1000 -> "${meters.toInt()} m"
        else -> String.format("%.2f km", meters / 1000)
    }
}

