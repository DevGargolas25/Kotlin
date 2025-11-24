package com.example.brigadist.ui.brigadist

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.brigadist.Orquestador
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.data.prefs.EmergencyPreferences
import com.example.brigadist.data.repository.FirebaseProfileRepository
import com.example.brigadist.ui.brigadist.components.*
import com.example.brigadist.ui.profile.model.FirebaseUserProfile
import com.example.brigadist.ui.sos.model.Emergency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrigadistEmergencyScreen(
    orquestador: Orquestador,
    selectedEmergency: Pair<String, Emergency>?,
    onEmergencyResolved: () -> Unit,
    onEmergencyAutoSelected: (Pair<String, Emergency>) -> Unit,
    emergencyPreferences: EmergencyPreferences,
    brigadistEmail: String
) {
    val context = LocalContext.current
    val emergencyRepository = remember { EmergencyRepository(context) }
    val profileRepository = remember { FirebaseProfileRepository() }
    
    // State for user medical information
    var userProfile by remember { mutableStateOf<FirebaseUserProfile?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResolveConfirmation by remember { mutableStateOf(false) }
    
    // Auto-select in-progress emergency if none is selected
    // This runs whenever selectedEmergency changes (including when it's null)
    DisposableEffect(selectedEmergency) {
        var hasSelected = false
        var listener: com.google.firebase.database.ValueEventListener? = null
        
        if (selectedEmergency == null) {
            // Check for in-progress emergencies assigned to this brigadist
            listener = emergencyRepository.listenToEmergenciesByStatus(
                listOf("In progress")
            ) { emergencyList ->
                if (!hasSelected) {
                    val assignedInProgress = emergencyList.filter { (_, emergency) ->
                        emergency.assignedBrigadistId == brigadistEmail &&
                        emergency.latitude != 0.0 && emergency.longitude != 0.0
                    }
                    
                    // Auto-select the first in-progress emergency if found
                    assignedInProgress.firstOrNull()?.let { emergency ->
                        hasSelected = true
                        onEmergencyAutoSelected(emergency)
                    }
                }
            }
        }
        
        onDispose {
            listener?.let { emergencyRepository.removeListener(it) }
        }
    }
    
    // Fetch user medical information when emergency is selected
    LaunchedEffect(selectedEmergency) {
        if (selectedEmergency != null) {
            // First, try to load from local storage (works offline)
            val savedProfile = emergencyPreferences.getUserProfile()
            if (savedProfile != null) {
                userProfile = savedProfile
                isLoading = false
            }
            
            val emergency = selectedEmergency.second
            val userEmail = emergency.userId
            
            if (userEmail.isNotEmpty()) {
                isLoading = true
                errorMessage = null
                
                // Try to fetch from Firebase (if online)
                profileRepository.getUserProfile(
                    email = userEmail,
                    onSuccess = { profile ->
                        userProfile = profile
                        isLoading = false
                        // Save to local storage for offline access
                        emergencyPreferences.saveSelectedEmergency(
                            emergencyKey = selectedEmergency.first,
                            emergency = emergency,
                            userProfile = profile,
                            brigadistEmail = brigadistEmail
                        )
                    },
                    onError = { error ->
                        // If offline and we have saved data, use that
                        if (savedProfile == null) {
                            errorMessage = error
                        }
                        isLoading = false
                    }
                )
            } else {
                errorMessage = "No user email found in emergency"
                isLoading = false
            }
        } else {
            userProfile = null
            errorMessage = null
        }
    }
    
    val emergency = selectedEmergency?.second
    val isMedicalEmergency = emergency?.emerType?.lowercase() == "medical"
    val userPhone = userProfile?.phone?.takeIf { it.isNotEmpty() }
    val showPhoneIcon = isMedicalEmergency && userPhone != null
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (selectedEmergency != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = emergency?.emerType ?: "Emergency",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    actions = {
                        if (showPhoneIcon) {
                            IconButton(
                                onClick = {
                                    val phoneNumber = "tel:${userPhone}"
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse(phoneNumber)
                                    }
                                    context.startActivity(dialIntent)
                                },
                                modifier = Modifier.semantics {
                                    contentDescription = "Call ${userProfile?.fullName ?: "user"}"
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call user"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        titleContentColor = MaterialTheme.colorScheme.onError,
                        actionIconContentColor = MaterialTheme.colorScheme.onError
                    )
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            if (selectedEmergency == null) {
                // Show "Select an emergency" message
                SelectEmergencyMessage()
            } else {
                // Show information based on emergency type
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Emergency header card
                    emergency?.let {
                        EmergencyHeaderCard(
                            emergency = it,
                            onResolveClick = {
                                showResolveConfirmation = true
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Loading state
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    // Error state
                    errorMessage?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "Error loading user information: $error",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Show medical information only for Medical emergencies
                    if (isMedicalEmergency) {
                        userProfile?.let { profile ->
                            // Emergency Contacts Section
                            SectionCard(
                                icon = Icons.Default.Phone,
                                iconTint = MaterialTheme.colorScheme.secondary,
                                title = "Emergency Contacts"
                            ) {
                                FieldRow(
                                    label = "Primary Contact",
                                    value = profile.emergencyName1
                                )
                                FieldRow(
                                    label = "Primary Phone",
                                    value = profile.emergencyPhone1
                                )
                                FieldRow(
                                    label = "Secondary Contact",
                                    value = profile.emergencyName2
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Medical Information Section
                            SectionCard(
                                icon = Icons.Default.FavoriteBorder,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = "Medical Information"
                            ) {
                                FieldRow(label = "Blood Type", value = profile.bloodType)
                                FieldRow(
                                    label = "Primary Physician",
                                    value = profile.doctorName
                                )
                                FieldRow(
                                    label = "Physician Phone",
                                    value = profile.doctorPhone
                                )
                                FieldRow(
                                    label = "Insurance Provider",
                                    value = profile.insuranceProvider
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Allergies Section
                            SectionCard(
                                icon = Icons.Default.Info,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = "Allergies"
                            ) {
                                FieldRow(
                                    label = "Food Allergies",
                                    value = profile.foodAllergies
                                )
                                FieldRow(
                                    label = "Environmental Allergies",
                                    value = profile.environmentalAllergies
                                )
                                FieldRow(
                                    label = "Drug Allergies",
                                    value = profile.drugAllergies
                                )
                                FieldRow(
                                    label = "Severity Notes",
                                    value = profile.severityNotes
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Current Medications Section
                            SectionCard(
                                icon = Icons.Default.ShoppingCart,
                                iconTint = MaterialTheme.colorScheme.secondary,
                                title = "Current Medications"
                            ) {
                                FieldRow(
                                    label = "Daily Medications",
                                    value = profile.dailyMedications
                                )
                                FieldRow(
                                    label = "Emergency Medications",
                                    value = profile.emergencyMedications
                                )
                                FieldRow(
                                    label = "Vitamins/Supplements",
                                    value = profile.vitaminsSupplements
                                )
                                FieldRow(
                                    label = "Special Instructions",
                                    value = profile.specialInstructions
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    } else {
                        // For Fire and Earthquake, show minimal information
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Emergency Reported",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "This emergency was reported by a bystander. Medical information is not available for this type of emergency.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Resolve confirmation dialog
    if (showResolveConfirmation) {
        AlertDialog(
            onDismissRequest = { showResolveConfirmation = false },
            title = {
                Text("Resolve Emergency")
            },
            text = {
                Text("Are you sure you want to mark this emergency as resolved?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedEmergency?.let { (key, _) ->
                            emergencyRepository.updateEmergencyStatus(
                                emergencyKey = key,
                                newStatus = "Resolved",
                                brigadistEmail = brigadistEmail,
                                onSuccess = {
                                    showResolveConfirmation = false
                                    onEmergencyResolved()
                                },
                                onError = { error ->
                                    errorMessage = "Failed to resolve emergency: $error"
                                    showResolveConfirmation = false
                                }
                            )
                        }
                    }
                ) {
                    Text("Resolve")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResolveConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

