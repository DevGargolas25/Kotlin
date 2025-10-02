package com.example.brigadist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.example.brigadist.auth.User
import com.example.brigadist.auth.credentialsToUser
import com.example.brigadist.ui.login.LoginScreen
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.brigadist.ui.chat.ChatScreen
import com.example.brigadist.ui.components.BrBottomBar
import com.example.brigadist.ui.components.Destination
import com.example.brigadist.ui.home.HomeRoute
import com.example.brigadist.ui.theme.BrigadistTheme
import com.example.brigadist.ui.theme.ThemeController

class MainActivity : ComponentActivity() {

    private lateinit var account: Auth0
    private var user by mutableStateOf<User?>(null)
    
    private fun login() {
        WebAuthProvider.login(account)
            .withScheme("com.example.brigadist")
            .withScope("openid profile email")
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    // Handle error
                }

                override fun onSuccess(result: Credentials) {
                    user = credentialsToUser(result)
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
                    user = null
                }
            })
    }
}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BrigadistApp() }
    }
}

@Composable
fun BrigadistApp() {
    val context = LocalContext.current
    val themeController = remember { ThemeController(context) }
    val themeState by themeController.themeState.collectAsState()
    
    
    // Handle app lifecycle for sensor management
    DisposableEffect(Unit) {
        themeController.onAppResumed()
        onDispose {
            themeController.onAppPaused()
        }
    }

    BrigadistTheme(darkTheme = themeState.isDark) {
        var selected by rememberSaveable { mutableStateOf(Destination.Home) }
        var selectedVideo by remember { mutableStateOf<VideoUi?>(null) }
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
                                onOpenProfile = { showProfile = true },    // <<< navigate to Profile
                                onNavigateToVideos = { selected = Destination.Videos }
                            )
                        } else {
                            ProfileScreen()                                // <<< show Profile
                        }
                    }
                    Destination.Chat -> ChatScreen()

                    Destination.Map    -> MapScreen()



                    Destination.Videos -> {
                        if (selectedVideo == null) {
                            VideosRoute(onVideoClick = { video -> selectedVideo = video })
                        } else {
                            VideoDetailScreen(video = selectedVideo!!, onBack = { selectedVideo = null })
                        }
                    }


        account = Auth0(
            getString(R.string.auth0_client_id),
            getString(R.string.auth0_domain)
        )

        setContent {
            BrigadistTheme {
                if (user == null) {
                    LoginScreen(onLoginClick = { login() })
                } else {
                    NavShell(user = user!!, onLogout = { logout() })
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

                override fun onSuccess(result: Credentials) {
                    user = credentialsToUser(result)
                }
            })
    }

        // SOS Confirmation Modal (Step 3)
        if (showSosConfirmationModal && selectedEmergencyType != null) {
            SosConfirmationModal(
                emergencyType = selectedEmergencyType!!,
                onDismiss = {
                    showSosConfirmationModal = false
                    selectedEmergencyType = null
                }

                override fun onSuccess(result: Void?) {
                    user = null
                }
            })
    }
}