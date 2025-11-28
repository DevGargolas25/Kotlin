package com.example.brigadist.ui.brigadist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brigadist.data.EmergencyRepository
import com.example.brigadist.utils.DistanceUtils
import com.example.brigadist.ui.sos.model.Emergency
import com.google.android.gms.maps.model.LatLng

@Composable
fun EmergencyTable(
    emergencies: List<Pair<String, Emergency>>,
    brigadistLocation: LatLng?,
    brigadistEmail: String,
    emergencyRepository: EmergencyRepository,
    onEmergencyAttended: () -> Unit,
    onEmergencySelected: (String, Emergency) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = emergencies,
            key = { it.first } // Use emergency key as stable key
        ) { (key, emergency) ->
            EmergencyTableRow(
                emergencyKey = key,
                emergency = emergency,
                brigadistLocation = brigadistLocation,
                brigadistEmail = brigadistEmail,
                emergencyRepository = emergencyRepository,
                onEmergencyAttended = onEmergencyAttended,
                onEmergencySelected = onEmergencySelected
            )
        }
    }
}

@Composable
fun EmergencyTableRow(
    emergencyKey: String,
    emergency: Emergency,
    brigadistLocation: LatLng?,
    brigadistEmail: String,
    emergencyRepository: EmergencyRepository,
    onEmergencyAttended: () -> Unit,
    onEmergencySelected: (String, Emergency) -> Unit
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    
    // Memoize distance calculation with stable keys
    val distance = remember(emergency.latitude, emergency.longitude, brigadistLocation?.latitude, brigadistLocation?.longitude) {
        brigadistLocation?.let { location ->
            val emergencyLocation = LatLng(emergency.latitude, emergency.longitude)
            DistanceUtils.calculateDistance(location, emergencyLocation)
        }
    }
    
    // Memoize formatted distance text
    val distanceText = remember(distance) {
        distance?.let { DistanceUtils.formatDistance(it) } ?: "N/A"
    }
    
    // Memoize emergency type display name
    val emergencyTypeText = remember(emergency.emerType) {
        when (emergency.emerType.lowercase()) {
            "fire" -> "Fire"
            "medical" -> "Medical"
            "earthquake" -> "Earthquake"
            else -> emergency.emerType
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Show confirmation dialog first
                showConfirmationDialog = true
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = emergencyTypeText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Location: ${emergency.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "away",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Confirmation dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text("Attend Emergency")
            },
            text = {
                Text("Are you sure you want to attend this ${emergencyTypeText.lowercase()} emergency?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        // First, attend the emergency
                        emergencyRepository.updateEmergencyStatus(
                            emergencyKey = emergencyKey,
                            newStatus = "In progress",
                            brigadistEmail = brigadistEmail,
                            onSuccess = {
                                onEmergencyAttended()
                                // Then navigate to emergency details
                                onEmergencySelected(emergencyKey, emergency)
                            },
                            onError = { /* Handle error */ }
                        )
                    }
                ) {
                    Text("Attend")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
