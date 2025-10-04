package com.example.brigadist.ui.profile.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseUserProfile(
    var bloodType: String = "",
    var dailyMedications: String = "",
    var doctorName: String = "",
    var doctorPhone: String = "",
    var drugAllergies: String = "",
    var email: String = "",
    var emergencyMedications: String = "",
    var emergencyName1: String = "",
    var emergencyName2: String = "",
    var emergencyPhone1: String = "",
    var emergencyPhone2: String = "",
    var environmentalAllergies: String = "",
    var foodAllergies: String = "",
    var fullName: String = "",
    var insuranceProvider: String = "",
    var phone: String = "",
    var severityNotes: String = "",
    var specialInstructions: String = "",
    var studentId: String = "",
    var userType: String = "",
    var vitaminsMedications: String = "",
    var vitaminsSupplements: String = ""
)
