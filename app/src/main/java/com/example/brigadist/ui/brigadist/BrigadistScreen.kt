package com.example.brigadist.ui.brigadist

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.brigadist.Orquestador
import com.example.brigadist.data.prefs.EmergencyPreferences
import com.example.brigadist.ui.brigadist.components.EmergencyTable
import com.example.brigadist.ui.brigadist.viewmodel.rememberBrigadistHomeState
import com.example.brigadist.ui.components.BrBottomBar
import com.example.brigadist.ui.components.Destination
import com.example.brigadist.ui.home.components.HomeNotificationBar
import com.example.brigadist.ui.profile.ui.profile.ProfileScreen
import com.example.brigadist.ui.sos.model.Emergency

@Composable
fun BrigadistScreen(
    orquestador: Orquestador,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val emergencyPreferences = remember { EmergencyPreferences(context) }
    val brigadistEmail = orquestador.getUserProfile().email
    
    var selected by rememberSaveable { mutableStateOf(Destination.Home) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var selectedEmergency by remember { mutableStateOf<Pair<String, Emergency>?>(null) }
    
    // Load saved emergency on startup (works offline)
    LaunchedEffect(Unit) {
        val savedEmergencyKey = emergencyPreferences.getSelectedEmergencyKey()
        val savedBrigadistEmail = emergencyPreferences.getBrigadistEmail()
        
        // Only restore if it was assigned to this brigadist
        if (savedEmergencyKey != null && savedBrigadistEmail == brigadistEmail) {
            val savedEmergency = emergencyPreferences.getSelectedEmergency()
            if (savedEmergency != null) {
                selectedEmergency = Pair(savedEmergencyKey, savedEmergency)
            }
        }
    }
    
    Scaffold(
        bottomBar = {
            BrBottomBar(
                selected = selected,
                onSelect = { dest ->
                    selected = dest
                    showProfile = false // Close profile when navigating to any destination
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
                                // Save emergency immediately (profile will be added later)
                                emergencyPreferences.saveSelectedEmergency(
                                    emergencyKey = key,
                                    emergency = emergency,
                                    userProfile = null,
                                    brigadistEmail = brigadistEmail
                                )
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
                            emergencyPreferences.clearSelectedEmergency()
                        },
                        onEmergencyAutoSelected = { emergency ->
                            selectedEmergency = emergency
                            // Save emergency immediately when auto-selected
                            emergencyPreferences.saveSelectedEmergency(
                                emergencyKey = emergency.first,
                                emergency = emergency.second,
                                userProfile = null,
                                brigadistEmail = brigadistEmail
                            )
                        },
                        emergencyPreferences = emergencyPreferences,
                        brigadistEmail = brigadistEmail
                    )
                }
                Destination.Videos -> {
                    // Videos screen placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Videos feature coming soon",
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
    val brigadistEmail = orquestador.getUserProfile().email
    
    // Use the state holder for Firebase listeners and location tracking
    val homeState = rememberBrigadistHomeState(context, brigadistEmail)
    
    // Connectivity monitoring
    var isOnline by remember { mutableStateOf(true) }
    var showOfflineAlert by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline = true
            }
            
            override fun onLost(network: Network) {
                isOnline = false
                // Show alert when going offline and there are no emergencies
                if (homeState.emergencies.isEmpty()) {
                    showOfflineAlert = true
                }
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                isOnline = hasInternet
                // Show/hide alert based on connectivity and emergency list
                if (!hasInternet && homeState.emergencies.isEmpty()) {
                    showOfflineAlert = true
                } else if (hasInternet) {
                    showOfflineAlert = false
                }
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        
        // Check initial connectivity
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        isOnline = hasInternet
        if (!hasInternet && homeState.emergencies.isEmpty()) {
            showOfflineAlert = true
        }
    }
    
    // Update alert visibility when emergencies list changes
    LaunchedEffect(homeState.emergencies.size) {
        if (homeState.emergencies.isEmpty() && !isOnline) {
            showOfflineAlert = true
        } else if (homeState.emergencies.isNotEmpty()) {
            showOfflineAlert = false
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
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
            if (homeState.hasInProgressEmergency) {
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
            
            if (homeState.emergencies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (homeState.hasInProgressEmergency) 
                            "No emergency in progress" 
                        else 
                            "No unattended emergencies",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                EmergencyTable(
                    emergencies = homeState.emergencies,
                    brigadistLocation = homeState.brigadistLocation,
                    brigadistEmail = brigadistEmail,
                    emergencyRepository = homeState.emergencyRepository,
                    isOnline = isOnline,
                    onEmergencyAttended = {
                        // Emergency status updated, will trigger re-fetch
                    },
                    onEmergencySelected = onEmergencySelected
                )
            }
        }
        
        // Offline alert when no emergencies
        AnimatedVisibility(
            visible = showOfflineAlert,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "No Internet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Please connect to the internet to see active emergencies",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showOfflineAlert = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

