package com.example.brigadist.ui.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.brigadist.ui.sos.components.EmergencyType
import com.example.brigadist.ui.sos.components.SosTypeRow

@Composable
fun SosSelectTypeModal(
    onDismiss: () -> Unit,
    onTypeSelected: (EmergencyType) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track modal opened
    LaunchedEffect(Unit) {
        SosTelemetry.trackSosSelectTypeOpened()
    }
    
    Dialog(
        onDismissRequest = {
            SosTelemetry.trackSosSelectTypeClosed()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Scrim background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { 
                    SosTelemetry.trackSosSelectTypeClosed()
                    onDismiss() 
                },
            contentAlignment = Alignment.Center
        ) {
            // Modal content
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .clickable(enabled = false) { }, // Prevent clicks from bubbling to scrim
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    // Red header band with close button
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header content
                        SosSelectTypeHeader()
                        
                        // Close button positioned in top-right
                        IconButton(
                            onClick = {
                                SosTelemetry.trackSosSelectTypeClosed()
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                    
                    // Divider
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    
                    // White content area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Emergency type rows
                        SosTypeRow(
                            emergencyType = EmergencyType.FIRE,
                            subtitle = "Report fire emergency or smoke detection",
                            onClick = {
                                SosTelemetry.trackSosTypeSelected(EmergencyType.FIRE)
                                onTypeSelected(EmergencyType.FIRE)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SosTypeRow(
                            emergencyType = EmergencyType.EARTHQUAKE,
                            subtitle = "Report seismic activity or structural damage",
                            onClick = {
                                SosTelemetry.trackSosTypeSelected(EmergencyType.EARTHQUAKE)
                                onTypeSelected(EmergencyType.EARTHQUAKE)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SosTypeRow(
                            emergencyType = EmergencyType.MEDICAL,
                            subtitle = "Report medical emergency or injury",
                            onClick = {
                                SosTelemetry.trackSosTypeSelected(EmergencyType.MEDICAL)
                                onTypeSelected(EmergencyType.MEDICAL)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Footer note
                        SosSelectTypeFooterNote()
                    }
                }
            }
        }
    }
}

@Composable
private fun SosSelectTypeHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Alert icon in circular chip
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    MaterialTheme.colorScheme.onError.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Emergency Alert",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select Emergency Type",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onError,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose the type of emergency to report",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onError.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SosSelectTypeFooterNote(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Emergency personnel will be notified immediately upon selection.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}
