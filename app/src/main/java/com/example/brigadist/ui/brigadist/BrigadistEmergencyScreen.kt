package com.example.brigadist.ui.brigadist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.brigadist.Orquestador
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.data.repository.FirebaseProfileRepository
import com.example.brigadist.ui.brigadist.components.*
import com.example.brigadist.ui.profile.model.FirebaseUserProfile
import com.example.brigadist.ui.sos.model.Emergency

@Composable
fun BrigadistEmergencyScreen(
    orquestador: Orquestador,
    selectedEmergency: Pair<String, Emergency>?,
    onEmergencyResolved: () -> Unit
) {
    val context = LocalContext.current
    val emergencyRepository = remember { EmergencyRepository(context) }
    val profileRepository = remember { FirebaseProfileRepository() }
    val brigadistEmail = orquestador.getUserProfile().email
    
    // State for user medical information
    var userProfile by remember { mutableStateOf<FirebaseUserProfile?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResolveConfirmation by remember { mutableStateOf(false) }
    
    // Fetch user medical information when emergency is selected
    LaunchedEffect(selectedEmergency) {
        if (selectedEmergency != null) {
            val emergency = selectedEmergency.second
            val userEmail = emergency.userId
            
            if (userEmail.isNotEmpty()) {
                isLoading = true
                errorMessage = null
                
                profileRepository.getUserProfile(
                    email = userEmail,
                    onSuccess = { profile ->
                        userProfile = profile
                        isLoading = false
                    },
                    onError = { error ->
                        errorMessage = error
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
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedEmergency == null) {
                // Show "Select an emergency" message
                SelectEmergencyMessage()
            } else {
                // Show medical information
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Emergency header
                    EmergencyHeaderCard(
                        emergency = selectedEmergency.second,
                        onResolveClick = {
                            showResolveConfirmation = true
                        }
                    )
                    
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
                    
                    // Medical information
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

