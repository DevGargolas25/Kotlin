package com.example.brigadist.ui.brigadist.viewmodel

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.ui.sos.model.Emergency
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay

/**
 * State holder for BrigadistHomeScreen that manages Firebase listeners and location updates.
 * This class encapsulates the business logic separated from the UI.
 */
class BrigadistHomeState(
    private val context: Context,
    private val brigadistEmail: String
) {
    val emergencyRepository = EmergencyRepository(context)
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private var inProgressListener: ValueEventListener? = null
    private var unattendedListener: ValueEventListener? = null
    
    var emergencies by mutableStateOf<List<Pair<String, Emergency>>>(emptyList())
        private set
    
    var brigadistLocation by mutableStateOf<LatLng?>(null)
        private set
    
    var hasInProgressEmergency by mutableStateOf(false)
        private set
    
    fun setupLocationUpdates() {
        // Location updates are handled via LaunchedEffect in the composable
    }
    
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
    
    fun setupFirebaseListeners() {
        // Use a local variable to track in-progress state between callbacks
        var currentHasInProgress = false
        
        // Check for "In progress" emergencies assigned to this brigadist
        inProgressListener = emergencyRepository.listenToEmergenciesByStatus(
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
        
        // Listen to "Unattended" emergencies only
        unattendedListener = emergencyRepository.listenToEmergenciesByStatus(
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
    }
    
    fun cleanup() {
        inProgressListener?.let { emergencyRepository.removeListener(it) }
        unattendedListener?.let { emergencyRepository.removeListener(it) }
    }
}

/**
 * Composable hook to use BrigadistHomeState with proper lifecycle management
 */
@Composable
fun rememberBrigadistHomeState(
    context: Context,
    brigadistEmail: String
): BrigadistHomeState {
    val state = remember {
        BrigadistHomeState(context, brigadistEmail)
    }
    
    // Setup Firebase listeners
    DisposableEffect(Unit) {
        state.setupFirebaseListeners()
        onDispose {
            state.cleanup()
        }
    }
    
    // Setup location updates
    LaunchedEffect(Unit) {
        state.updateLocation()
        
        // Update location every 5 seconds
        while (true) {
            delay(5000)
            state.updateLocation()
        }
    }
    
    return state
}
