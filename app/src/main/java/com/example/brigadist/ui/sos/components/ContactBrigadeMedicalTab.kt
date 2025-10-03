package com.example.brigadist.ui.sos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brigadist.Orquestador

@Composable
fun ContactBrigadeMedicalTab(
    orquestador: Orquestador,
    modifier: Modifier = Modifier
) {
    val emergencyContact = orquestador.getEmergencyContact()
    val medicalInfo = orquestador.getMedicalInfo()
    val allergies = orquestador.getAllergies()
    val medications = orquestador.getMedications()
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Emergency Contacts Section
        SectionCard(
            icon = Icons.Default.Phone,
            iconTint = MaterialTheme.colorScheme.secondary,
            title = "Emergency Contacts"
        ) {
            FieldRow(label = "Primary Contact", value = emergencyContact.primaryContactName)
            FieldRow(label = "Primary Phone", value = emergencyContact.primaryContactPhone)
            FieldRow(label = "Secondary Contact", value = emergencyContact.secondaryContactName)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Medical Information Section
        SectionCard(
            icon = Icons.Default.FavoriteBorder,
            iconTint = MaterialTheme.colorScheme.primary,
            title = "Medical Information"
        ) {
            FieldRow(label = "Blood Type", value = medicalInfo.bloodType)
            FieldRow(label = "Primary Physician", value = medicalInfo.primaryPhysician)
            FieldRow(label = "Physician Phone", value = medicalInfo.physicianPhone)
            FieldRow(label = "Insurance Provider", value = medicalInfo.insuranceProvider)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Allergies Section
        SectionCard(
            icon = Icons.Default.Info,
            iconTint = MaterialTheme.colorScheme.primary,
            title = "Allergies"
        ) {
            FieldRow(label = "Food Allergies", value = allergies.foodAllergies)
            FieldRow(label = "Environmental Allergies", value = allergies.environmentalAllergies)
            FieldRow(label = "Drug Allergies", value = allergies.drugAllergies)
            FieldRow(label = "Severity Notes", value = allergies.severityNotes)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Current Medications Section
        SectionCard(
            icon = Icons.Default.ShoppingCart,
            iconTint = MaterialTheme.colorScheme.secondary,
            title = "Current Medications"
        ) {
            FieldRow(label = "Daily Medications", value = medications.dailyMedications)
            FieldRow(label = "Emergency Medications", value = medications.emergencyMedications)
            FieldRow(label = "Vitamins/Supplements", value = medications.vitaminsSupplements)
            FieldRow(label = "Special Instructions", value = medications.specialInstructions)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * A card component grouping related profile fields. Displays an icon and a title at the
 * top followed by arbitrary content. The card uses a white background, subtle border
 * and rounded corners similar to the design in the uploaded images.
 */
@Composable
fun SectionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColour = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColour),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Displays a single labelled field within a profile section. The label appears above
 * a rounded surface containing the value. Empty values are represented by an
 * unobtrusive placeholder.
 */
@Composable
fun FieldRow(label: String, value: String) {
    val labelColour = MaterialTheme.colorScheme.onSurface
    val valueColour = MaterialTheme.colorScheme.onSurface
    val fieldBackground = MaterialTheme.colorScheme.surfaceVariant
    
    Text(
        text = label,
        color = labelColour,
        style = MaterialTheme.typography.labelLarge
    )
    Spacer(modifier = Modifier.height(4.dp))
    Surface(
        shape = MaterialTheme.shapes.small,
        color = fieldBackground,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = value.ifEmpty { "Not specified" },
            color = if (value.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else valueColour,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}
