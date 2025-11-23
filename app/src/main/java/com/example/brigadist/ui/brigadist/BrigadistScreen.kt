package com.example.brigadist.ui.brigadist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.brigadist.Orquestador
import com.example.brigadist.ui.components.BrBottomBar
import com.example.brigadist.ui.components.Destination
import com.example.brigadist.ui.home.components.HomeNotificationBar
import com.example.brigadist.ui.profile.ui.profile.ProfileScreen

@Composable
fun BrigadistScreen(
    orquestador: Orquestador,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by rememberSaveable { mutableStateOf(Destination.Home) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showPlaceholderMessage by remember { mutableStateOf<String?>(null) }
    
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
                        // Home screen with header
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            // Header with HomeScreen text and profile icon - only on Home screen
                            HomeNotificationBar(
                                text = "HomeScreen",
                                onBellClick = { /* Notifications placeholder */ },
                                onMenuClick = { showProfile = true }
                            )
                            Spacer(Modifier.height(8.dp))
                            
                            // Home content
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "This will be the home",
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
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
                Destination.Emergency,
                Destination.Map,
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

