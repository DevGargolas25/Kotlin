package com.example.brigadist.ui.profile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.brigadist.ui.profile.model.FirebaseUserProfile

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val email: String,
    val bloodType: String = "",
    val dailyMedications: String = "",
    val doctorName: String = "",
    val doctorPhone: String = "",
    val drugAllergies: String = "",
    val emergencyMedications: String = "",
    val emergencyName1: String = "",
    val emergencyName2: String = "",
    val emergencyPhone1: String = "",
    val emergencyPhone2: String = "",
    val environmentalAllergies: String = "",
    val foodAllergies: String = "",
    val fullName: String = "",
    val insuranceProvider: String = "",
    val phone: String = "",
    val severityNotes: String = "",
    val specialInstructions: String = "",
    val studentId: String = "",
    val userType: String = "",
    val vitaminsMedications: String = "",
    val vitaminsSupplements: String = ""
) {
    fun toFirebaseUserProfile(): FirebaseUserProfile {
        return FirebaseUserProfile(
            bloodType = bloodType,
            dailyMedications = dailyMedications,
            doctorName = doctorName,
            doctorPhone = doctorPhone,
            drugAllergies = drugAllergies,
            email = email,
            emergencyMedications = emergencyMedications,
            emergencyName1 = emergencyName1,
            emergencyName2 = emergencyName2,
            emergencyPhone1 = emergencyPhone1,
            emergencyPhone2 = emergencyPhone2,
            environmentalAllergies = environmentalAllergies,
            foodAllergies = foodAllergies,
            fullName = fullName,
            insuranceProvider = insuranceProvider,
            phone = phone,
            severityNotes = severityNotes,
            specialInstructions = specialInstructions,
            studentId = studentId,
            userType = userType,
            vitaminsMedications = vitaminsMedications,
            vitaminsSupplements = vitaminsSupplements
        )
    }
    
    companion object {
        fun fromFirebaseUserProfile(profile: FirebaseUserProfile): UserProfileEntity {
            return UserProfileEntity(
                email = profile.email,
                bloodType = profile.bloodType,
                dailyMedications = profile.dailyMedications,
                doctorName = profile.doctorName,
                doctorPhone = profile.doctorPhone,
                drugAllergies = profile.drugAllergies,
                emergencyMedications = profile.emergencyMedications,
                emergencyName1 = profile.emergencyName1,
                emergencyName2 = profile.emergencyName2,
                emergencyPhone1 = profile.emergencyPhone1,
                emergencyPhone2 = profile.emergencyPhone2,
                environmentalAllergies = profile.environmentalAllergies,
                foodAllergies = profile.foodAllergies,
                fullName = profile.fullName,
                insuranceProvider = profile.insuranceProvider,
                phone = profile.phone,
                severityNotes = profile.severityNotes,
                specialInstructions = profile.specialInstructions,
                studentId = profile.studentId,
                userType = profile.userType,
                vitaminsMedications = profile.vitaminsMedications,
                vitaminsSupplements = profile.vitaminsSupplements
            )
        }
    }
}

