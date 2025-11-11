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
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.example.brigadist.auth.User
import com.example.brigadist.auth.credentialsToUser
import com.example.brigadist.auth.OfflineCredentialsManager
import com.example.brigadist.ui.chat.ChatScreen
import com.example.brigadist.ui.login.OfflineLoginScreen
import com.example.brigadist.ui.login.SetOfflinePasswordScreen
import com.example.brigadist.ui.login.ReLoginPromptDialog
import com.example.brigadist.ui.components.BrBottomBar
import com.example.brigadist.ui.components.Destination
import com.example.brigadist.ui.home.HomeRoute
import com.example.brigadist.ui.login.LoginScreen
import com.example.brigadist.ui.map.MapScreen
import com.example.brigadist.ui.profile.ui.profile.ProfileScreen
import com.example.brigadist.ui.sos.SosModal
import com.example.brigadist.ui.sos.SosSelectTypeModal
import com.example.brigadist.ui.sos.SosConfirmationModal
import com.example.brigadist.ui.sos.SosReconnectionModal
import com.example.brigadist.ui.sos.SosContactBrigadeScreen
import com.example.brigadist.ui.sos.components.EmergencyType
import com.example.brigadist.data.PendingEmergencyStore
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.ui.platform.LocalContext
import com.example.brigadist.ui.theme.BrigadistTheme
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.brigadist.ui.videos.VideoDetailScreen
import com.example.brigadist.ui.videos.VideosRoute
import com.example.brigadist.ui.videos.model.Video
import com.example.brigadist.ui.analytics.AnalyticsHomeScreen
import com.google.firebase.FirebaseApp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.example.brigadist.ui.theme.GreenSecondary

class MainActivity : ComponentActivity() {

    private lateinit var account: Auth0
    private var user by mutableStateOf<User?>(null)
    private var isAnalyticsUser by mutableStateOf(false)
    private var isOfflineMode by mutableStateOf(false)
    private var isOnline by mutableStateOf(true)
    private var showSetOfflinePassword by mutableStateOf(false)
    private var showReLoginPrompt by mutableStateOf(false)
    private var offlineLoginError by mutableStateOf<String?>(null)
    private lateinit var credentialsManager: CredentialsManager
    private lateinit var offlineCredentialsManager: OfflineCredentialsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase (maintaining firebase functionality)
        FirebaseApp.initializeApp(this)

        account = Auth0(
            getString(R.string.auth0_client_id),
            getString(R.string.auth0_domain)
        )
        credentialsManager = CredentialsManager(AuthenticationAPIClient(account), SharedPreferencesStorage(this))
        offlineCredentialsManager = OfflineCredentialsManager(this)
        
        // Check initial connectivity
        checkConnectivity()

        credentialsManager.getCredentials(object : Callback<Credentials, CredentialsManagerException> {
            override fun onSuccess(result: Credentials) {
                user = credentialsToUser(result)
                
                // Check userType for existing credentials
                user?.let { currentUser ->
                    val orquestador = Orquestador(currentUser, this@MainActivity, isOfflineMode = false)
                    // Wait a moment for Firebase data to load, then check userType
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        val userType = orquestador.getUserType().lowercase()
                        isAnalyticsUser = userType == "analytics" || userType == "analitics"
                    }, 1000) // 1 second delay to allow Firebase to load
                }
            }

            override fun onFailure(error: CredentialsManagerException) {
                // No credentials stored
            }
        })

        setContent {
            when {
                // Show set offline password screen after first Auth0 login
                showSetOfflinePassword && user != null -> {
                    SetOfflinePasswordScreen(
                        userEmail = user!!.email,
                        onPasswordSet = { password ->
                            offlineCredentialsManager.saveOfflineCredentials(user!!.email, password)
                            showSetOfflinePassword = false
                        },
                        onSkip = {
                            showSetOfflinePassword = false
                        }
                    )
                }
                // No user logged in
                user == null -> {
                    if (isOnline) {
                        // Online: Show normal Auth0 login
                        LoginScreen(onLoginClick = { login() })
                    } else {
                        // Offline: Show offline login if credentials are set up
                        if (offlineCredentialsManager.hasOfflineCredentials()) {
                            OfflineLoginScreen(
                                onOfflineLoginClick = { email, password ->
                                    handleOfflineLogin(email, password)
                                },
                                errorMessage = offlineLoginError,
                                storedEmail = offlineCredentialsManager.getStoredEmail()
                            )
                        } else {
                            // No offline credentials set up
                            NoOfflineAccessScreen()
                        }
                    }
                }
                // User logged in - show appropriate screen
                isAnalyticsUser -> {
                    BrigadistTheme(darkTheme = false) {
                        AnalyticsHomeScreen(onLogout = { logout() })
                    }
                }
                else -> {
                    BrigadistApp(
                        orquestador = Orquestador(user!!, this@MainActivity, isOfflineMode = isOfflineMode),
                        onLogout = { logout() },
                        isOfflineMode = isOfflineMode,
                        showReLoginPrompt = showReLoginPrompt,
                        onReLoginClick = {
                            showReLoginPrompt = false
                            logout()
                        },
                        onReLoginDismiss = {
                            showReLoginPrompt = false
                        }
                    )
                }
            }
        }
    }

    private fun login() {
        WebAuthProvider.login(account)
            .withScheme("com.example.brigadist")
            .withScope("openid profile email offline_access")
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    // Handle error
                }

                override fun onSuccess(result: Credentials) {
                    credentialsManager.saveCredentials(result)
                    user = credentialsToUser(result)
                    isOfflineMode = false
                    
                    // Check userType after successful login
                    user?.let { currentUser ->
                        val orquestador = Orquestador(currentUser, this@MainActivity, isOfflineMode = false)
                        // Wait a moment for Firebase data to load, then check userType
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            val userType = orquestador.getUserType().lowercase()
                            isAnalyticsUser = userType == "analytics" || userType == "analitics"
                            
                            // Check if offline password is set up, if not, prompt to set it
                            if (!offlineCredentialsManager.hasOfflineCredentials()) {
                                showSetOfflinePassword = true
                            }
                        }, 1000) // 1 second delay to allow Firebase to load
                    }
                }
            })
    }
    
    private fun checkConnectivity() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        isOnline = hasInternet
        
        // Register network callback for continuous monitoring
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val wasOffline = !isOnline
                isOnline = true
                
                // If we just came back online and user is in offline mode, prompt to re-login
                if (wasOffline && isOfflineMode && user != null) {
                    showReLoginPrompt = true
                }
            }
            
            override fun onLost(network: Network) {
                isOnline = false
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                val wasOffline = !isOnline
                isOnline = hasInternet
                
                // If we just came back online and user is in offline mode, prompt to re-login
                if (wasOffline && isOnline && isOfflineMode && user != null) {
                    showReLoginPrompt = true
                }
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, callback)
    }
    
    private fun handleOfflineLogin(email: String, password: String) {
        if (offlineCredentialsManager.validateOfflineCredentials(email, password)) {
            // Create a mock user for offline mode
            user = User(
                id = "offline_$email",
                name = email.substringBefore("@"),
                email = email,
                picture = ""
            )
            isOfflineMode = true
            offlineLoginError = null
        } else {
            offlineLoginError = "Invalid credentials"
        }
    }

    private fun logout() {
        if (isOfflineMode) {
            // Just clear user state for offline mode
            user = null
            isOfflineMode = false
            isAnalyticsUser = false
        } else {
            // Normal Auth0 logout
            WebAuthProvider.logout(account)
                .withScheme("com.example.brigadist")
                .start(this, object : Callback<Void?, AuthenticationException> {
                    override fun onFailure(error: AuthenticationException) {
                        // Handle error
                    }

                    override fun onSuccess(result: Void?) {
                        credentialsManager.clearCredentials()
                        user = null
                        isAnalyticsUser = false
                        isOfflineMode = false
                    }
                })
        }
    }
}

@Composable
fun NoOfflineAccessScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenSecondary)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = "No Connection",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Internet Connection",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You need to set up offline access before you can use the app without an internet connection.\n\nPlease connect to the internet and log in to set up offline access.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun BrigadistApp(
    orquestador: Orquestador,
    onLogout: () -> Unit,
    isOfflineMode: Boolean = false,
    showReLoginPrompt: Boolean = false,
    onReLoginClick: () -> Unit = {},
    onReLoginDismiss: () -> Unit = {}
) {
    // Collect theme state from Orchestrator's ThemeControlle
    val themeState by orquestador.themeController.themeState.collectAsState()
    val context = LocalContext.current
    
    // Connectivity and pending emergency management
    var isOffline by rememberSaveable { mutableStateOf(false) }
    var showReconnectionModal by rememberSaveable { mutableStateOf(false) }
    var wasOffline by rememberSaveable { mutableStateOf(false) }
    val pendingEmergencyStore = remember { PendingEmergencyStore(context) }

    // Connectivity monitoring
    LaunchedEffect(Unit) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOffline = false
                // Check if we just came back online and have a pending emergency
                if (wasOffline && pendingEmergencyStore.hasPendingEmergency()) {
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
                if (wasOfflineBefore && !isOffline && pendingEmergencyStore.hasPendingEmergency()) {
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

    // Lifecycle management for theme controller
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> orquestador.themeController.onAppResumed()
                Lifecycle.Event.ON_PAUSE -> orquestador.themeController.onAppPaused()
                else -> { /* No action needed */ }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BrigadistTheme(darkTheme = themeState.isDark) {
        var selected by rememberSaveable { mutableStateOf(Destination.Home) }
        var selectedVideo by remember { mutableStateOf<Video?>(null) }
        var showChatDetail by rememberSaveable { mutableStateOf(false) }
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
                                userName = orquestador.getUserProfile().fullName,
                                onOpenProfile = { showProfile = true },
                                onNavigateToVideos = { selected = Destination.Videos },
                                onOpenVideo = { video -> selectedVideo = video },
                                onVideoClickFromCarousel = { video ->
                                    selectedVideo = video
                                    selected = Destination.Videos
                                }
                            )
                        } else {
                            ProfileScreen(orquestador = orquestador, onLogout = onLogout)
                        }
                    }

                    Destination.Chat -> ChatScreen()

                    Destination.Map -> MapScreen(orquestador)

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
                onContactBrigade = { showContactBrigadeScreen = true }
            )
        }

        // SOS Select Type Modal
        if (showSosSelectTypeModal) {
            SosSelectTypeModal(
                onDismiss = { showSosSelectTypeModal = false },
                onTypeSelected = { emergencyType ->
                    selectedEmergencyType = emergencyType
                    showSosConfirmationModal = true
                },
                orquestador = orquestador
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
        
        // Reconnection Modal - appears when connection is restored and there's a pending emergency
        if (showReconnectionModal) {
            SosReconnectionModal(
                onSend = {
                    // Firebase persistence will automatically sync the queued emergency
                    // Clear the pending flag since we're confirming the send
                    pendingEmergencyStore.clearPendingEmergency()
                    showReconnectionModal = false
                },
                onDismiss = {
                    // User chose not to send, clear the pending flag
                    pendingEmergencyStore.clearPendingEmergency()
                    showReconnectionModal = false
                }
            )
        }

        // Contact Brigade Screen
        if (showContactBrigadeScreen) {
            SosContactBrigadeScreen(
                orquestador = orquestador,
                onBack = { showContactBrigadeScreen = false }
            )
        }
        
        // Re-login prompt when connection is restored
        if (showReLoginPrompt) {
            ReLoginPromptDialog(
                onLoginClick = onReLoginClick,
                onDismiss = onReLoginDismiss
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