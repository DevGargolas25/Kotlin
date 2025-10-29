package com.example.brigadist.ui.profile

import com.example.brigadist.ui.profile.model.Allergies
import com.example.brigadist.ui.profile.model.EmergencyContact
import com.example.brigadist.ui.profile.model.FirebaseUserProfile
import com.example.brigadist.ui.profile.model.MedicalInfo
import com.example.brigadist.ui.profile.model.Medications
import com.example.brigadist.ui.profile.model.UserProfile

interface ProfileView {
    fun showLoading()
    fun hideLoading()
    fun showProfile(profile: FirebaseUserProfile) // Keep for backward compatibility
    fun showProfileData(
        firebaseProfile: FirebaseUserProfile,
        userProfile: UserProfile,
        emergencyContact: EmergencyContact,
        medicalInfo: MedicalInfo,
        allergies: Allergies,
        medications: Medications
    )
    fun showError(message: String)
    fun showSuccess(message: String)
}
