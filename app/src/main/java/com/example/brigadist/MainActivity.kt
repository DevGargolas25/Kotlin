package com.example.brigadist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.example.brigadist.auth.User
import com.example.brigadist.auth.credentialsToUser
import com.example.brigadist.screens.DetailChat
import com.example.brigadist.ui.chat.ChatScreen
import com.example.brigadist.ui.components.BrBottomBar
import com.example.brigadist.ui.components.Destination
import com.example.brigadist.ui.home.HomeRoute
import com.example.brigadist.ui.login.LoginScreen
import com.example.brigadist.ui.map.MapScreen
import com.example.brigadist.ui.profile.ProfileScreen
import com.example.brigadist.ui.sos.SosModal
import com.example.brigadist.ui.sos.SosSelectTypeModal
import com.example.brigadist.ui.sos.SosConfirmationModal
import com.example.brigadist.ui.sos.components.EmergencyType
import com.example.brigadist.ui.theme.BrigadistTheme
import com.example.brigadist.ui.videos.VideoDetailScreen
import com.example.brigadist.ui.videos.VideosRoute
import com.example.brigadist.ui.videos.model.VideoUi
import com.google.firebase.FirebaseApp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

class MainActivity : ComponentActivity() {

    private lateinit var account: Auth0
    private lateinit var orchestrator: AppOrchestrator
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase (maintaining firebase functionality)
        FirebaseApp.initializeApp(this)

        account = Auth0(
            getString(R.string.auth0_client_id),
            getString(R.string.auth0_domain)
        )
        
        orchestrator = AppOrchestrator(this)
        
        setContent {
            val appState by orchestrator.appState.collectAsState()
            
            if (!appState.isReady) {
                LoginScreen(onLoginClick = { login() })
            } else {
                BrigadistApp()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        orchestrator.onAppResumed()
    }
    
    override fun onPause() {
        super.onPause()
        orchestrator.onAppPaused()
    }
    
    private fun login() {
        WebAuthProvider.login(account)
            .withScheme("com.example.brigadist")
            .withScope("openid profile email")
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    // Handle error
                }

                override fun onSuccess(result: Credentials) {
                    orchestrator.setUser(credentialsToUser(result))
                }
            })
    }

    private fun logout() {
        WebAuthProvider.logout(account)
            .withScheme("com.example.brigadist")
            .start(this, object : Callback<Void?, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    // Handle error
                }

                override fun onSuccess(result: Void?) {
                    orchestrator.setUser(null)
                }
            })
    }
}

@Composable
fun BrigadistApp() {
    BrigadistTheme {
        var selected by rememberSaveable { mutableStateOf(Destination.Home) }
        var selectedVideo by remember { mutableStateOf<VideoUi?>(null) }
        var showChatDetail by rememberSaveable { mutableStateOf(false) }
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
                        if (dest == Destination.Home) showProfile = false
                        if (dest == Destination.Chat) showChatDetail = false
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
                                onOpenProfile = { showProfile = true },
                                onNavigateToVideos = { selected = Destination.Videos }
                            )
                        } else {
                            ProfileScreen()
                        }
                    }

                    Destination.Chat -> {
                        if (!showChatDetail) {
                            ChatScreen(
                                onOpenConversation = { showChatDetail = true }
                            )
                        } else {
                            DetailChat(
                                onBack = { showChatDetail = false }
                            )
                        }
                    }

                    Destination.Map -> MapScreen()

                    Destination.Videos -> {
                        if (selectedVideo == null) {
                            VideosRoute(onVideoClick = { video -> selectedVideo = video })
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
                onSendEmergencyAlert = { showSosSelectTypeModal = true },
                onContactBrigade = {
                    selected = Destination.Chat
                    showChatDetail = true
                }
            )
        }

        // SOS Select Type Modal
        if (showSosSelectTypeModal) {
            SosSelectTypeModal(
                onDismiss = { showSosSelectTypeModal = false },
                onTypeSelected = { emergencyType ->
                    selectedEmergencyType = emergencyType
                    showSosConfirmationModal = true
                }
            )
        }

        // SOS Confirmation Modal
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