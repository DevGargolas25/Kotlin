package com.example.brigadist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.brigadist.auth.User
import com.example.brigadist.ui.chat.ChatScreen
import com.example.brigadist.ui.components.BrBottomBar
import com.example.brigadist.ui.components.Destination
import com.example.brigadist.ui.home.HomeRoute
import com.example.brigadist.ui.map.MapScreen
import com.example.brigadist.ui.profile.ProfileScreen
import com.example.brigadist.ui.sos.SosConfirmationModal
import com.example.brigadist.ui.sos.SosModal
import com.example.brigadist.ui.sos.SosSelectTypeModal
import com.example.brigadist.ui.sos.components.EmergencyType
import com.example.brigadist.ui.theme.BrigadistTheme
import com.example.brigadist.ui.videos.VideoDetailScreen
import com.example.brigadist.ui.videos.VideosRoute
import com.example.brigadist.ui.videos.model.Video

@Composable
fun NavShell(
    user: User, 
    orchestrator: AppOrchestrator,
    onLogout: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val orquestador = Orquestador(user, context)
    val themeState by orchestrator.themeState.collectAsState()

    BrigadistTheme(darkTheme = themeState.isDark) {
        var selected by rememberSaveable { mutableStateOf(Destination.Home) }
        var selectedVideo by remember { mutableStateOf<Video?>(null) }
        var showProfile by rememberSaveable { mutableStateOf(false) }
        var showSosModal by rememberSaveable { mutableStateOf(false) }
        var showSosSelectTypeModal by rememberSaveable { mutableStateOf(false) }
        var showSosConfirmationModal by rememberSaveable { mutableStateOf(false) }
        var selectedEmergencyType by remember { mutableStateOf<EmergencyType?>(null) }
        Scaffold(
            bottomBar = {
                BrBottomBar(
                    selected = selected,
                    onSelect = { dest ->
                        selected = dest
                        orchestrator.trackScreenView(dest.name)
                        // reset inner states when switching tabs
                        if (dest == Destination.Home) showProfile = false
                    },
                    onSosClick = { showSosModal = true }
                )
            }
        ) { inner ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                color = MaterialTheme.colorScheme.background
            ) {
                when (selected) {
                    Destination.Home -> {
                        if (!showProfile) {
                            HomeRoute(
                                onOpenProfile = { 
                                    showProfile = true 
                                    orchestrator.trackScreenView("Profile")
                                },    // <<< navigate to Profile
                                onNavigateToVideos = { selected = Destination.Videos },
                                onOpenVideo = { video -> selectedVideo = video }
                            )
                        } else {
                            ProfileScreen(orquestador = orquestador, onLogout = onLogout)
                        }
                    }
                    Destination.Chat -> ChatScreen()

                    Destination.Map    -> MapScreen(orquestador = orquestador)



                    Destination.Videos -> {
                        if (selectedVideo == null) {
                            VideosRoute(
                                onVideoClick = { video -> selectedVideo = video }
                            )
                        } else {
                            VideoDetailScreen(video = selectedVideo!!, onBack = { selectedVideo = null })
                        }
                    }


                }
            }
        }

        // SOS Modal
        if (showSosModal) {
            SosModal(
                onDismiss = { showSosModal = false },
                onSendEmergencyAlert = {
                    // Show Step 2: Select Emergency Type modal
                    showSosSelectTypeModal = true
                },
                onContactBrigade = {
                    // Navigate to brigade contact or placeholder
                    selected = Destination.Chat
                }
            )
        }

        // SOS Select Type Modal (Step 2)
        if (showSosSelectTypeModal) {
            SosSelectTypeModal(
                onDismiss = { showSosSelectTypeModal = false },
                onTypeSelected = { emergencyType ->
                    // Show Step 3: Confirmation modal
                    selectedEmergencyType = emergencyType
                    showSosConfirmationModal = true
                }
            )
        }

        // SOS Confirmation Modal (Step 3)
        if (showSosConfirmationModal && selectedEmergencyType != null) {
            SosConfirmationModal(
                emergencyType = selectedEmergencyType!!,
                onDismiss = { 
                    showSosConfirmationModal = false
                    selectedEmergencyType = null
                }
            )
        }
    }
}

@Composable
private fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
