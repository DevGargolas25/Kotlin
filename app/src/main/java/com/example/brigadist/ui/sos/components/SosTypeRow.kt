package com.example.brigadist.ui.sos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.theme.*

enum class EmergencyType(val displayName: String, val icon: ImageVector) {
    FIRE("Fire Alert", Icons.Default.Warning),
    EARTHQUAKE("Earthquake Alert", Icons.Default.Info),
    MEDICAL("Medical Alert", Icons.Default.Favorite)
}

@Composable
fun SosTypeRow(
    emergencyType: EmergencyType,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in circular chip with category-specific colors
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        getAlertContainerColor(emergencyType),
                        CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = getAlertAccentColor(emergencyType).copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = emergencyType.icon,
                    contentDescription = emergencyType.displayName,
                    tint = getAlertOnContainerColor(emergencyType),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = emergencyType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// Helper functions to get category-specific colors
@Composable
private fun getAlertContainerColor(emergencyType: EmergencyType): Color {
    return when (emergencyType) {
        EmergencyType.FIRE -> AlertFireContainer
        EmergencyType.EARTHQUAKE -> AlertEarthquakeContainer
        EmergencyType.MEDICAL -> AlertMedicalContainer
    }
}

@Composable
private fun getAlertAccentColor(emergencyType: EmergencyType): Color {
    return when (emergencyType) {
        EmergencyType.FIRE -> AlertFireAccent
        EmergencyType.EARTHQUAKE -> AlertEarthquakeAccent
        EmergencyType.MEDICAL -> AlertMedicalAccent
    }
}

@Composable
private fun getAlertOnContainerColor(emergencyType: EmergencyType): Color {
    return when (emergencyType) {
        EmergencyType.FIRE -> OnAlertFireContainer
        EmergencyType.EARTHQUAKE -> OnAlertEarthquakeContainer
        EmergencyType.MEDICAL -> OnAlertMedicalContainer
    }
}
