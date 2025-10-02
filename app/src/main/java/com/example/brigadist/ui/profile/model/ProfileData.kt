package com.example.brigadist.ui.profile.model

data class UserProfile(
    val fullName: String,
    val studentId: String,
    val email: String,
    val phone: String
)

data class EmergencyContact(
    val primaryContactName: String,
    val primaryContactPhone: String,
    val secondaryContactName: String
)

data class MedicalInfo(
    val bloodType: String,
    val primaryPhysician: String,
    val physicianPhone: String,
    val insuranceProvider: String
)

data class Allergies(
    val foodAllergies: String,
    val environmentalAllergies: String,
    val drugAllergies: String,
    val severityNotes: String
)

data class Medications(
    val dailyMedications: String,
    val emergencyMedications: String,
    val vitaminsSupplements: String,
    val specialInstructions: String
)