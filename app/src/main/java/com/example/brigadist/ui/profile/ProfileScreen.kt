package com.example.brigadist.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.brigadist.Orquestador
import com.example.brigadist.ui.profile.model.FirebaseUserProfile
import com.example.brigadist.ui.profile.model.UserProfile

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    orquestador: Orquestador,
    onLogout: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    val firebaseUserProfile = orquestador.firebaseUserProfile
    val userProfile = orquestador.getUserProfile()

    if (isEditing) {
        val profileToEdit = firebaseUserProfile ?: FirebaseUserProfile(
            fullName = userProfile.fullName,
            email = userProfile.email
        )

        EditProfileScreen(
            profile = profileToEdit,
            onSave = { updatedProfile ->
                orquestador.updateUserProfile(updatedProfile)
                isEditing = false
            },
            onCancel = { isEditing = false }
        )
    } else {
        val scrollState = rememberScrollState()
        val emergencyContact = orquestador.getEmergencyContact()
        val medicalInfo = orquestador.getMedicalInfo()
        val allergies = orquestador.getAllergies()
        val medications = orquestador.getMedications()

        Scaffold(
            modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                ProfileHeader(userProfile = userProfile, onEdit = { isEditing = true })
                Spacer(modifier = Modifier.height(16.dp))

                SectionCard(
                    icon = Icons.Default.Person,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Personal Information"
                ) {
                    FieldRow(label = "Full Name", value = userProfile.fullName)
                    FieldRow(label = "Student ID", value = userProfile.studentId)
                    FieldRow(label = "Email", value = userProfile.email)
                    FieldRow(label = "Phone", value = userProfile.phone)
                }
                Spacer(modifier = Modifier.height(16.dp))

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

                Button(onClick = onLogout) {
                    Text("Log Out")
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(userProfile: UserProfile, onEdit: () -> Unit = {}) {
    val avatarBackground = MaterialTheme.colorScheme.primaryContainer
    val avatarTint = MaterialTheme.colorScheme.primary
    val nameColour = MaterialTheme.colorScheme.onSurface
    val roleColour = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(avatarBackground, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = avatarTint,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = userProfile.fullName,
                color = nameColour,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Student Brigade Member",
                color = roleColour,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit profile",
                tint = Color(0xFFB4A4C0)
            )
        }
    }
}

@Composable
fun SectionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColour = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    Surface(
        shape = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(12.dp),
        color = fieldBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (value.isNotBlank()) value else " ",
            color = valueColour,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 12.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}
