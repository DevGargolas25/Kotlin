package com.example.brigadist

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.brigadist.auth.User
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.ui.chat.ChatScreen
import com.example.brigadist.ui.components.BrBottomBar
import com.example.brigadist.ui.components.Destination
import com.example.brigadist.ui.home.HomeRoute
import com.example.brigadist.ui.map.MapScreen
import com.example.brigadist.ui.profile.ui.profile.ProfileScreen
import com.example.brigadist.ui.sos.SosConfirmationModal
import com.example.brigadist.ui.sos.SosModal
import com.example.brigadist.ui.sos.SosReconnectionModal
import com.example.brigadist.ui.sos.SosSelectTypeModal
import com.example.brigadist.ui.sos.components.EmergencyType
import com.example.brigadist.ui.theme.BrigadistTheme
import com.example.brigadist.ui.news.NewsDetailScreen
import com.example.brigadist.ui.news.NewsRoute
import com.example.brigadist.ui.news.model.News
import com.example.brigadist.ui.videos.VideoDetailScreen
import com.example.brigadist.ui.videos.VideosRoute
import com.example.brigadist.ui.videos.model.Video

@Composable
fun NavShell(
    user: User,
    orchestrator: AppOrchestrator,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val orquestador = Orquestador(user, context)
    val themeState by orchestrator.themeState.collectAsState()
    
    // Connectivity and pending emergency management
    var isOffline by rememberSaveable { mutableStateOf(false) }
    var showReconnectionModal by rememberSaveable { mutableStateOf(false) }
    var wasOffline by rememberSaveable { mutableStateOf(false) }
    var isSyncing by rememberSaveable { mutableStateOf(false) }
    val emergencyRepository = remember { EmergencyRepository(context) }

    // Connectivity monitoring
    LaunchedEffect(Unit) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOffline = false
                // Check if we just came back online and have a pending emergency
                if (wasOffline && emergencyRepository.hasPendingEmergencies()) {
                    showReconnectionModal = true
                }
                wasOffline = false
            }
            
            override fun onLost(network: Network) {
                isOffline = true
                wasOffline = true
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                val wasOfflineBefore = isOffline
                isOffline = !hasInternet
                
                // If we just came back online and have pending emergency
                if (wasOfflineBefore && !isOffline && emergencyRepository.hasPendingEmergencies()) {
                    showReconnectionModal = true
                    wasOffline = false
                } else if (isOffline) {
                    wasOffline = true
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
        isOffline = !hasInternet
        wasOffline = isOffline
    }

    val emergencyPreferences = remember { com.example.brigadist.data.prefs.EmergencyPreferences(context) }
    
    BrigadistTheme(darkTheme = themeState.isDark) {
        var selected by rememberSaveable { mutableStateOf(Destination.Home) }
        var selectedVideo by remember { mutableStateOf<Video?>(null) }
        var selectedNews by remember { mutableStateOf<News?>(null) }
        var showProfile by rememberSaveable { mutableStateOf(false) }
        var showSosModal by rememberSaveable { mutableStateOf(false) }
        var showSosSelectTypeModal by rememberSaveable { mutableStateOf(false) }
        var showSosConfirmationModal by rememberSaveable { mutableStateOf(false) }
        var showContactBrigadeScreen by rememberSaveable { mutableStateOf(false) }
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
                        if (dest != Destination.News) selectedNews = null
                        if (dest != Destination.Videos) selectedVideo = null
                    },
                    onSosClick = { 
                        // Check if there's an active medical emergency first
                        if (emergencyPreferences.hasActiveMedicalEmergency()) {
                            showContactBrigadeScreen = true
                        } else {
                            showSosModal = true
                        }
                    }
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
                                userName = orquestador.getUserProfile().fullName,
                                onOpenProfile = {
                                    showProfile = true
                                    orchestrator.trackScreenView("Profile")
                                },    // <<< navigate to Profile
                                onNavigateToVideos = { selected = Destination.Videos },
                                onNavigateToNews = { selected = Destination.News },
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
                    
                    Destination.News -> {
                        if (selectedNews == null) {
                            NewsRoute(
                                onNewsClick = { news -> selectedNews = news }
                            )
                        } else {
                            NewsDetailScreen(
                                news = selectedNews!!,
                                onBack = { selectedNews = null }
                            )
                        }
                    }
                    
                    Destination.Emergency -> {
                        // Emergency destination (used in Brigadist view)
                        // Regular users use SOS button instead
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
                },
                orquestador = orquestador
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
        
        // Reconnection Modal - appears when connection is restored and there's a pending emergency
        if (showReconnectionModal) {
            SosReconnectionModal(
                onSend = {
                    // Sync pending emergencies from cache
                    isSyncing = true
                    emergencyRepository.syncPendingEmergencies { successCount, failureCount, totalCount ->
                        isSyncing = false
                        showReconnectionModal = false
                        // Optionally show a toast or snackbar with sync results
                    }
                },
                onDismiss = {
                    // User chose not to send, keep emergencies in cache for later
                    showReconnectionModal = false
                }
            )
        }
        
        // Contact Brigade Screen
        if (showContactBrigadeScreen) {
            com.example.brigadist.ui.sos.SosContactBrigadeScreen(
                orquestador = orquestador,
                onBack = { 
                    showContactBrigadeScreen = false
                    selected = Destination.Home
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