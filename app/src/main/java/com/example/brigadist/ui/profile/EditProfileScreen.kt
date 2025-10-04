package com.example.brigadist.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.brigadist.analytics.AnalyticsHelper
import com.example.brigadist.ui.profile.model.FirebaseUserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profile: FirebaseUserProfile,
    onSave: (FirebaseUserProfile) -> Unit,
    onCancel: () -> Unit
) {
    var editedProfile by remember { mutableStateOf(profile.copy()) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        AnalyticsHelper.trackProfileUpdate()
                        onSave(editedProfile) 
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Personal Information
            Text("Personal Information", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            EditTextField(label = "Full Name", value = editedProfile.fullName, onValueChange = { editedProfile = editedProfile.copy(fullName = it) })
            EditTextField(label = "Student ID", value = editedProfile.studentId, onValueChange = { editedProfile = editedProfile.copy(studentId = it) })
            EditTextField(label = "Email", value = editedProfile.email, onValueChange = { editedProfile = editedProfile.copy(email = it) })
            EditTextField(label = "Phone", value = editedProfile.phone, onValueChange = { editedProfile = editedProfile.copy(phone = it) })

            Spacer(modifier = Modifier.height(16.dp))

            // Emergency Contacts
            Text("Emergency Contacts", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            EditTextField(label = "Primary Contact Name", value = editedProfile.emergencyName1, onValueChange = { editedProfile = editedProfile.copy(emergencyName1 = it) })
            EditTextField(label = "Primary Contact Phone", value = editedProfile.emergencyPhone1, onValueChange = { editedProfile = editedProfile.copy(emergencyPhone1 = it) })
            EditTextField(label = "Secondary Contact Name", value = editedProfile.emergencyName2, onValueChange = { editedProfile = editedProfile.copy(emergencyName2 = it) })
            EditTextField(label = "Secondary Contact Phone", value = editedProfile.emergencyPhone2, onValueChange = { editedProfile = editedProfile.copy(emergencyPhone2 = it) })

            Spacer(modifier = Modifier.height(16.dp))

            // Medical Information
            Text("Medical Information", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            EditTextField(label = "Blood Type", value = editedProfile.bloodType, onValueChange = { editedProfile = editedProfile.copy(bloodType = it) })
            EditTextField(label = "Doctor's Name", value = editedProfile.doctorName, onValueChange = { editedProfile = editedProfile.copy(doctorName = it) })
            EditTextField(label = "Doctor's Phone", value = editedProfile.doctorPhone, onValueChange = { editedProfile = editedProfile.copy(doctorPhone = it) })
            EditTextField(label = "Insurance Provider", value = editedProfile.insuranceProvider, onValueChange = { editedProfile = editedProfile.copy(insuranceProvider = it) })

            Spacer(modifier = Modifier.height(16.dp))

            // Allergies
            Text("Allergies", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            EditTextField(label = "Food Allergies", value = editedProfile.foodAllergies, onValueChange = { editedProfile = editedProfile.copy(foodAllergies = it) })
            EditTextField(label = "Environmental Allergies", value = editedProfile.environmentalAllergies, onValueChange = { editedProfile = editedProfile.copy(environmentalAllergies = it) })
            EditTextField(label = "Drug Allergies", value = editedProfile.drugAllergies, onValueChange = { editedProfile = editedProfile.copy(drugAllergies = it) })
            EditTextField(label = "Severity Notes", value = editedProfile.severityNotes, onValueChange = { editedProfile = editedProfile.copy(severityNotes = it) })

            Spacer(modifier = Modifier.height(16.dp))

            // Current Medications
            Text("Current Medications", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            EditTextField(label = "Daily Medications", value = editedProfile.dailyMedications, onValueChange = { editedProfile = editedProfile.copy(dailyMedications = it) })
            EditTextField(label = "Emergency Medications", value = editedProfile.emergencyMedications, onValueChange = { editedProfile = editedProfile.copy(emergencyMedications = it) })
            EditTextField(label = "Vitamins/Medications", value = editedProfile.vitaminsMedications, onValueChange = { editedProfile = editedProfile.copy(vitaminsMedications = it) })
            EditTextField(label = "Vitamins/Supplements", value = editedProfile.vitaminsSupplements, onValueChange = { editedProfile = editedProfile.copy(vitaminsSupplements = it) })
            EditTextField(label = "Special Instructions", value = editedProfile.specialInstructions, onValueChange = { editedProfile = editedProfile.copy(specialInstructions = it) })

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EditTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true
    )
}
