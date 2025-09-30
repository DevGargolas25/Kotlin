package com.example.brigadist.ui.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import com.example.brigadist.ui.theme.*

@Composable
fun SosConfirmationModal(
    emergencyType: EmergencyType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Track modal shown
    LaunchedEffect(emergencyType) {
        SosTelemetry.trackSosConfirmationShown(emergencyType)
    }
    
    Dialog(
        onDismissRequest = {
            SosTelemetry.trackSosConfirmationDismissed("back")
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
                    SosTelemetry.trackSosConfirmationDismissed("scrim")
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
                    // Green header band with close button
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header content
                        SosConfirmationHeader()
                        
                        // Close button positioned in top-right
                        IconButton(
                            onClick = {
                                SosTelemetry.trackSosConfirmationDismissed("x")
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
                                tint = Color.White
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
                        // Type-specific confirmation message
                        SosConfirmationMessage(emergencyType)
                    }
                }
            }
        }
    }
}

@Composable
private fun SosConfirmationHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GreenSecondary) // Success green header
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success icon in circular chip
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    Color.White.copy(alpha = 0.15f), // Light tint over green
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Alert Sent Successfully",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Emergency personnel have been notified",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SosConfirmationMessage(
    emergencyType: EmergencyType,
    modifier: Modifier = Modifier
) {
    val message = when (emergencyType) {
        EmergencyType.FIRE -> "The fire emergency has been reported to the corresponding personnel."
        EmergencyType.EARTHQUAKE -> "The earthquake emergency has been reported to the corresponding personnel."
        EmergencyType.MEDICAL -> "The medical emergency has been reported to the corresponding personnel."
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AlertMedicalContainer.copy(alpha = 0.3f) // Very light success container
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = OnAlertMedicalContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}
