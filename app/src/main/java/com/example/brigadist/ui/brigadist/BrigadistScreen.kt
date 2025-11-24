package com.example.brigadist.ui.brigadist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.brigadist.Orquestador
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.ui.components.BrBottomBar
import com.example.brigadist.ui.components.Destination
import com.example.brigadist.ui.home.components.HomeNotificationBar
import com.example.brigadist.ui.profile.ui.profile.ProfileScreen
import com.example.brigadist.ui.sos.model.Emergency
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun BrigadistScreen(
    orquestador: Orquestador,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by rememberSaveable { mutableStateOf(Destination.Home) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showPlaceholderMessage by remember { mutableStateOf<String?>(null) }
    var selectedEmergency by rememberSaveable { mutableStateOf<Pair<String, Emergency>?>(null) }
    
    Scaffold(
        bottomBar = {
            BrBottomBar(
                selected = selected,
                onSelect = { dest ->
                    selected = dest
                    showProfile = false // Close profile when navigating to any destination
                    // Show placeholder message based on destination (except Chat which has its own screen)
                    showPlaceholderMessage = when (dest) {
                        Destination.Home -> null
                        Destination.Chat -> null // Chat has its own screen
                        Destination.Emergency -> "Here will be the emergency"
                        Destination.Map -> "Here will be the map"
                        Destination.Videos -> "Here will be the videos"
                    }
                },
                useEmergencyAsDestination = true,
                onSosClick = {} // Not used when useEmergencyAsDestination is true
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selected) {
                Destination.Home -> {
                    if (!showProfile) {
                        BrigadistHomeScreen(
                            orquestador = orquestador,
                            onMenuClick = { showProfile = true },
                            onEmergencySelected = { key, emergency ->
                                selectedEmergency = Pair(key, emergency)
                                selected = Destination.Emergency
                            }
                        )
                    } else {
                        // Profile screen - reuse existing ProfileScreen component (has its own Scaffold)
                        ProfileScreen(
                            orquestador = orquestador,
                            onLogout = onLogout
                        )
                    }
                }
                Destination.Chat -> {
                    // Chat screen
                    BrigadistChatScreen()
                }
                Destination.Map -> {
                    BrigadistMapScreen(
                        orquestador = orquestador
                    )
                }
                Destination.Emergency -> {
                    BrigadistEmergencyScreen(
                        orquestador = orquestador,
                        selectedEmergency = selectedEmergency,
                        onEmergencyResolved = {
                            selectedEmergency = null
                        }
                    )
                }
                Destination.Videos -> {
                    // Show placeholder message for selected destination
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = showPlaceholderMessage ?: "Here will be the ${selected.name.lowercase()}",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BrigadistHomeScreen(
    orquestador: Orquestador,
    onMenuClick: () -> Unit,
    onEmergencySelected: (String, Emergency) -> Unit
) {
    val context = LocalContext.current
    val emergencyRepository = remember { EmergencyRepository(context) }
    val brigadistEmail = orquestador.getUserProfile().email
    
    // State for emergencies
    var emergencies by remember { mutableStateOf<List<Pair<String, Emergency>>>(emptyList()) }
    var brigadistLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasInProgressEmergency by remember { mutableStateOf(false) }
    
    // Get brigadist's current location - update periodically
    LaunchedEffect(Unit) {
        val fusedLocationClient: FusedLocationProviderClient = 
            LocationServices.getFusedLocationProviderClient(context)
        
        // Function to get and update location
        suspend fun updateLocation() {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        brigadistLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Handle permission denied - location will remain null
            }
        }
        
        // Get initial location
        updateLocation()
        
        // Update location every 5 seconds to ensure we have current location
        while (true) {
            delay(5000)
            updateLocation()
        }
    }
    
    // Listen to emergencies - only fetch "Unattended" status as requested
    // Also check for "In progress" assigned to this brigadist separately
    DisposableEffect(Unit) {
        // Use a local variable to track in-progress state between callbacks
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
            
            // If there's an in-progress emergency, show only that one
            if (currentHasInProgress) {
                emergencies = assignedInProgress
            }
        }
        
        // Listen to "Unattended" emergencies only (as requested)
        val unattendedListener = emergencyRepository.listenToEmergenciesByStatus(
            listOf("Unattended")
        ) { emergencyList ->
            // Check current state - only update if we don't have an in-progress emergency
            if (!hasInProgressEmergency) {
                // Filter out emergencies without valid latitude/longitude
                val validEmergencies = emergencyList.filter { (_, emergency) ->
                    // Only include emergencies with valid coordinates (not 0.0, 0.0)
                    emergency.latitude != 0.0 && emergency.longitude != 0.0 &&
                    emergency.status == "Unattended"
                }
                
                emergencies = validEmergencies
            }
        }
        
        onDispose {
            emergencyRepository.removeListener(inProgressListener)
            emergencyRepository.removeListener(unattendedListener)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        HomeNotificationBar(
            text = "HomeScreen",
            onBellClick = { /* Notifications placeholder */ },
            onMenuClick = onMenuClick
        )
        Spacer(Modifier.height(8.dp))
        
        // Emergency table
        if (hasInProgressEmergency) {
            Text(
                text = "Emergency In Progress",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Text(
                text = "Unattended Emergencies",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        if (emergencies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (hasInProgressEmergency) 
                        "No emergency in progress" 
                    else 
                        "No unattended emergencies",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            EmergencyTable(
                emergencies = emergencies,
                brigadistLocation = brigadistLocation,
                brigadistEmail = brigadistEmail,
                emergencyRepository = emergencyRepository,
                onEmergencyAttended = {
                    // Emergency status updated, will trigger re-fetch
                },
                onEmergencySelected = onEmergencySelected
            )
        }
    }
}

@Composable
fun EmergencyTable(
    emergencies: List<Pair<String, Emergency>>,
    brigadistLocation: LatLng?,
    brigadistEmail: String,
    emergencyRepository: EmergencyRepository,
    onEmergencyAttended: () -> Unit,
    onEmergencySelected: (String, Emergency) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(emergencies) { (key, emergency) ->
            EmergencyTableRow(
                emergencyKey = key,
                emergency = emergency,
                brigadistLocation = brigadistLocation,
                brigadistEmail = brigadistEmail,
                emergencyRepository = emergencyRepository,
                onEmergencyAttended = onEmergencyAttended,
                onEmergencySelected = onEmergencySelected
            )
        }
    }
}

@Composable
fun EmergencyTableRow(
    emergencyKey: String,
    emergency: Emergency,
    brigadistLocation: LatLng?,
    brigadistEmail: String,
    emergencyRepository: EmergencyRepository,
    onEmergencyAttended: () -> Unit,
    onEmergencySelected: (String, Emergency) -> Unit
) {
    val distance = remember(emergency, brigadistLocation) {
        brigadistLocation?.let { location ->
            val emergencyLocation = LatLng(emergency.latitude, emergency.longitude)
            calculateDistance(location, emergencyLocation)
        }
    }
    
    val distanceText = distance?.let { formatDistance(it) } ?: "N/A"
    
    // Get emergency type display name
    val emergencyTypeText = when (emergency.emerType.lowercase()) {
        "fire" -> "🔥 Fire"
        "medical" -> "🏥 Medical"
        "earthquake" -> "🌍 Earthquake"
        else -> emergency.emerType
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // First, attend the emergency
                emergencyRepository.updateEmergencyStatus(
                    emergencyKey = emergencyKey,
                    newStatus = "In progress",
                    brigadistEmail = brigadistEmail,
                    onSuccess = {
                        onEmergencyAttended()
                        // Then navigate to emergency details
                        onEmergencySelected(emergencyKey, emergency)
                    },
                    onError = { /* Handle error */ }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = emergencyTypeText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Location: ${emergency.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "away",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
