package com.example.brigadist.ui.brigadist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.brigadist.ui.news.NewsRoute
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
                Destination.News -> {
                    NewsRoute()
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
                onEmergencyAttended = {
                    // Emergency status updated, will trigger re-fetch
                },
                onEmergencySelected = onEmergencySelected
            )
        }
    }
}

