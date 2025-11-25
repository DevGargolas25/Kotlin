package com.example.brigadist.ui.sos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brigadist.Orquestador
import com.example.brigadist.R
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.data.FirebaseUserAdapter
import com.example.brigadist.data.prefs.EmergencyPreferences
import com.example.brigadist.ui.chat.ChatScreen
import com.example.brigadist.ui.sos.components.ContactBrigadeLocationTab
import com.example.brigadist.ui.sos.components.ContactBrigadeMedicalTab
import com.example.brigadist.ui.sos.components.EmergencyType
import com.example.brigadist.ui.sos.model.Emergency
import com.google.firebase.database.ValueEventListener

enum class ContactBrigadeTab {
    Location, Assistant, Medical
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosContactBrigadeScreen(
    orquestador: Orquestador,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val emergencyRepository = remember { EmergencyRepository(context) }
    val emergencyPreferences = remember { EmergencyPreferences(context) }
    val userAdapter = remember { FirebaseUserAdapter() }
    
    var selectedTab by remember { mutableStateOf(ContactBrigadeTab.Location) }
    var ChatUsed by remember { mutableStateOf(false) }
    var emergencyKey by remember { mutableStateOf<String?>(null) }
    var currentEmergency by remember { mutableStateOf<Emergency?>(null) }
    var brigadistPhoneNumber by remember { mutableStateOf<String?>(null) }
    var isOnline by remember { mutableStateOf(emergencyRepository.isOnline()) }
    
    var showDistanceWarning by remember { mutableStateOf<Pair<Double, () -> Unit>?>(null) }
    var showCancelConfirmation by remember { mutableStateOf(false) }
    
    // Get emergency key - either from preferences (existing) or create new one
    LaunchedEffect(Unit) {
        val savedKey = emergencyPreferences.getActiveMedicalEmergencyKey()
        if (savedKey != null) {
            // Load existing emergency
            emergencyKey = savedKey
            // Try to load from Firebase
            if (emergencyRepository.isOnline()) {
                emergencyRepository.getEmergencyByKey(
                    emergencyKey = savedKey,
                    onSuccess = { emergency ->
                        currentEmergency = emergency
                        // If resolved, clear it
                        if (emergency?.status == "Resolved") {
                            emergencyPreferences.clearActiveMedicalEmergency()
                            emergencyKey = null
                            currentEmergency = null
                        } else if (emergency != null) {
                            // If in progress, fetch brigadist phone
                            if (emergency.status == "In progress" && emergency.assignedBrigadistId.isNotEmpty()) {
                                fetchBrigadistPhone(emergency.assignedBrigadistId, userAdapter) { phone ->
                                    brigadistPhoneNumber = phone
                                }
                            }
                        }
                    },
                    onError = { 
                        // Offline or error - load from saved preferences if available
                        val savedEmergency = emergencyPreferences.getSelectedEmergency()
                        if (savedEmergency != null && savedEmergency.emerType.lowercase() == "medical") {
                            currentEmergency = savedEmergency
                        }
                    }
                )
            } else {
                // Offline - load from saved preferences
                val savedEmergency = emergencyPreferences.getSelectedEmergency()
                if (savedEmergency != null && savedEmergency.emerType.lowercase() == "medical") {
                    currentEmergency = savedEmergency
                }
            }
        } else {
            // Create new medical emergency
            EmergencyActions.createAndSaveEmergency(
                context = context,
                emergencyType = EmergencyType.MEDICAL,
                emergencyRepository = emergencyRepository,
                orquestador = orquestador,
                chatUsed = ChatUsed,
                onSuccess = { key ->
                    emergencyKey = key
                    emergencyPreferences.saveActiveMedicalEmergency(key)
                    isOnline = emergencyRepository.isOnline()
                    // Try to get the emergency data to save
                    emergencyRepository.getEmergencyByKey(
                        emergencyKey = key,
                        onSuccess = { emergency ->
                            if (emergency != null) {
                                currentEmergency = emergency
                                emergencyPreferences.saveSelectedEmergency(
                                    emergencyKey = key,
                                    emergency = emergency,
                                    userProfile = null,
                                    brigadistEmail = ""
                                )
                            }
                        },
                        onError = { }
                    )
                },
                onError = { },
                onOffline = { 
                    // Offline - try to get the key from cache or use a generated one
                    // Emergency will be saved in cache and synced later
                },
                onDistanceWarning = { distance, proceed ->
                    showDistanceWarning = Pair(distance, proceed)
                }
            )
        }
        
        // Update online status periodically
        while (true) {
            delay(2000)
            isOnline = emergencyRepository.isOnline()
        }
    }
    
    // Listen to emergency status changes
    var emergencyListener by remember { mutableStateOf<ValueEventListener?>(null) }
    
    DisposableEffect(emergencyKey) {
        if (emergencyKey != null && emergencyRepository.isOnline()) {
            val listener = emergencyRepository.listenToEmergencyByKey(emergencyKey!!) { emergency ->
                currentEmergency = emergency
                
                // Update saved emergency in preferences
                if (emergency != null) {
                    emergencyPreferences.saveSelectedEmergency(
                        emergencyKey = emergencyKey!!,
                        emergency = emergency,
                        userProfile = null,
                        brigadistEmail = emergency.assignedBrigadistId
                    )
                    
                    // If resolved, clear active emergency
                    if (emergency.status == "Resolved") {
                        emergencyPreferences.clearActiveMedicalEmergency()
                        emergencyKey = null
                        currentEmergency = null
                    } 
                    // If status changed to "In progress", fetch brigadist phone
                    else if (emergency.status == "In progress" && emergency.assignedBrigadistId.isNotEmpty()) {
                        fetchBrigadistPhone(emergency.assignedBrigadistId, userAdapter) { phone ->
                            brigadistPhoneNumber = phone
                        }
                    }
                    // If status changed back to "Unattended", clear brigadist phone
                    else if (emergency.status == "Unattended") {
                        brigadistPhoneNumber = null
                    }
                }
            }
            emergencyListener = listener
        }
        
        onDispose {
            emergencyListener?.let { emergencyRepository.removeListener(it) }
        }
    }
    
    // Determine phone number to use
    val phoneNumberToUse = remember(currentEmergency, brigadistPhoneNumber) {
        if (currentEmergency?.status == "In progress" && brigadistPhoneNumber != null) {
            brigadistPhoneNumber!!
        } else {
            "1234567890" // Default emergency number
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {},
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Contact Brigade",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Back to emergency options"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val phoneNumber = "tel:$phoneNumberToUse"
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse(phoneNumber)
                            }
                            context.startActivity(dialIntent)
                        },
                        modifier = Modifier.semantics {
                            contentDescription = if (currentEmergency?.status == "In progress") {
                                "Call assigned brigadist"
                            } else {
                                "Call emergency number"
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    titleContentColor = MaterialTheme.colorScheme.onError,
                    navigationIconContentColor = MaterialTheme.colorScheme.onError,
                    actionIconContentColor = MaterialTheme.colorScheme.onError
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Offline alert banner
            if (!isOnline) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Please connect to the internet to update the status of your emergency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.semantics {
                    contentDescription = "Contact Brigade tabs"
                }
            ) {
                ContactBrigadeTab.values().forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            if (tab == ContactBrigadeTab.Assistant) {
                                ChatUsed = true
                                emergencyKey?.let { key ->
                                    if (isOnline) {
                                        emergencyRepository.updateChatUsed(key)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.semantics {
                            contentDescription = when (tab) {
                                ContactBrigadeTab.Location -> "Location tab - View emergency status and information"
                                ContactBrigadeTab.Assistant -> "Assistant tab - Chat with emergency AI assistant"
                                ContactBrigadeTab.Medical -> "Medical tab - View medical information"
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    ContactBrigadeTab.Location -> Icons.Default.LocationOn
                                    ContactBrigadeTab.Assistant -> Icons.Default.Person
                                    ContactBrigadeTab.Medical -> Icons.Default.Favorite
                                },
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                text = when (tab) {
                                    ContactBrigadeTab.Location -> stringResource(R.string.contact_brigade_tab_location)
                                    ContactBrigadeTab.Assistant -> stringResource(R.string.contact_brigade_tab_assistant)
                                    ContactBrigadeTab.Medical -> stringResource(R.string.contact_brigade_tab_medical)
                                }
                            )
                        }
                    )
                }
            }
            
            // Tab Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    ContactBrigadeTab.Location -> {
                        ContactBrigadeLocationTab(
                            emergency = currentEmergency,
                            isOnline = isOnline,
                            onCancelEmergency = {
                                showCancelConfirmation = true
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ContactBrigadeTab.Assistant -> {
                        ChatScreen()
                    }
                    ContactBrigadeTab.Medical -> {
                        ContactBrigadeMedicalTab(
                            orquestador = orquestador,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
    
    // Show distance warning dialog if needed
    showDistanceWarning?.let { (distance, proceed) ->
        DistanceWarningDialog(
            distanceInMeters = distance,
            onProceed = {
                proceed()
                showDistanceWarning = null
            },
            onCancel = {
                showDistanceWarning = null
            }
        )
    }
    
    // Cancel emergency confirmation dialog
    if (showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmation = false },
            title = {
                Text("Cancel Emergency")
            },
            text = {
                Text("Are you sure you want to cancel this emergency? This will mark it as resolved and you will be taken back to the home screen.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirmation = false
                        emergencyKey?.let { key ->
                            if (isOnline) {
                                emergencyRepository.resolveEmergency(
                                    emergencyKey = key,
                                    onSuccess = {
                                        // Clear active emergency
                                        emergencyPreferences.clearActiveMedicalEmergency()
                                        emergencyPreferences.clearSelectedEmergency()
                                        emergencyKey = null
                                        currentEmergency = null
                                        // Navigate back
                                        onBack()
                                    },
                                    onError = { error ->
                                        // Error resolving - could show error message
                                        // For now, just close dialog
                                    }
                                )
                            } else {
                                // Offline - still clear local state and navigate back
                                // Status will sync when online
                                emergencyPreferences.clearActiveMedicalEmergency()
                                emergencyPreferences.clearSelectedEmergency()
                                emergencyKey = null
                                currentEmergency = null
                                onBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Emergency")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelConfirmation = false }
                ) {
                    Text("Keep Emergency")
                }
            }
        )
    }
}

private fun fetchBrigadistPhone(
    brigadistEmail: String,
    userAdapter: FirebaseUserAdapter,
    onPhoneFetched: (String) -> Unit
) {
    userAdapter.findUserByEmail(
        email = brigadistEmail,
        onResult = { foundUser ->
            if (foundUser != null) {
                val phone = foundUser.data["phone"] as? String
                if (!phone.isNullOrEmpty()) {
                    onPhoneFetched(phone)
                }
            }
        },
        onError = { }
    )
}
