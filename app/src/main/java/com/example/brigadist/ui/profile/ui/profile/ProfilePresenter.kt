package com.example.brigadist.ui.profile

import android.content.Context
import com.example.brigadist.cache.ImageCacheManager
import com.example.brigadist.data.repository.ProfileRepository
import com.example.brigadist.ui.profile.model.Allergies
import com.example.brigadist.ui.profile.model.EmergencyContact
import com.example.brigadist.ui.profile.model.FirebaseUserProfile
import com.example.brigadist.ui.profile.model.MedicalInfo
import com.example.brigadist.ui.profile.model.Medications
import com.example.brigadist.ui.profile.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class ProfilePresenter(
    private val view: ProfileView,
    private val repository: ProfileRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val context: Context? = null
) {

    fun loadProfile(email: String) {
        view.showLoading()
        
        // Use coroutines to load profile data with multithreading
        coroutineScope.launch {
            try {
                // Load Firebase profile on IO thread (network operation)
                val firebaseProfile = withContext(Dispatchers.IO) {
                    suspendCancellableCoroutine<FirebaseUserProfile?> { continuation ->
                        repository.getUserProfile(email,
                            onSuccess = { profile ->
                                continuation.resume(profile)
                            },
                            onError = { msg ->
                                continuation.resume(null)
                            }
                        )
                    }
                }

                if (firebaseProfile == null) {
                    withContext(Dispatchers.Main) {
                        view.hideLoading()
                        view.showError("No se encontró el perfil del usuario.")
                    }
                    return@launch
                }

                // Process different sections in parallel on background threads
                val userProfileDeferred = async(Dispatchers.Default) {
                    mapToUserProfile(firebaseProfile, email)
                }
                
                val emergencyContactDeferred = async(Dispatchers.Default) {
                    mapToEmergencyContact(firebaseProfile)
                }
                
                val medicalInfoDeferred = async(Dispatchers.Default) {
                    mapToMedicalInfo(firebaseProfile)
                }
                
                val allergiesDeferred = async(Dispatchers.Default) {
                    mapToAllergies(firebaseProfile)
                }
                
                val medicationsDeferred = async(Dispatchers.Default) {
                    mapToMedications(firebaseProfile)
                }

                // Wait for all parallel operations to complete
                val userProfile = userProfileDeferred.await()
                val emergencyContact = emergencyContactDeferred.await()
                val medicalInfo = medicalInfoDeferred.await()
                val allergies = allergiesDeferred.await()
                val medications = medicationsDeferred.await()

                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showProfileData(
                        firebaseProfile = firebaseProfile,
                        userProfile = userProfile,
                        emergencyContact = emergencyContact,
                        medicalInfo = medicalInfo,
                        allergies = allergies,
                        medications = medications
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error al cargar el perfil: ${e.message ?: "Error desconocido"}")
                }
            }
        }
    }

    fun saveProfile(profile: FirebaseUserProfile) {
        view.showLoading()
        
        coroutineScope.launch {
            try {
                // Save profile on IO thread (network operation)
                val success = withContext(Dispatchers.IO) {
                    suspendCancellableCoroutine<Boolean> { continuation ->
                        repository.updateUserProfile(profile,
                            onSuccess = {
                                continuation.resume(true)
                            },
                            onError = { msg ->
                                continuation.resume(false)
                            }
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    if (success) {
                        // Invalidate image cache if profile image URL changed
                        // This ensures updated profile images are reloaded
                        context?.let {
                            // If profile has imageUrl field, invalidate it
                            // For now, we'll invalidate all profile-related images
                            // You can add specific image URL invalidation if needed
                            ImageCacheManager.getInstance(it).clearMemoryCache()
                        }
                        view.showSuccess("Perfil actualizado correctamente.")
                    } else {
                        view.showError("Error al actualizar el perfil.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Error al guardar: ${e.message ?: "Error desconocido"}")
                }
            }
        }
    }

    // Helper functions to map FirebaseUserProfile to different data structures
    private fun mapToUserProfile(profile: FirebaseUserProfile, defaultEmail: String): UserProfile {
        return UserProfile(
            fullName = profile.fullName.ifEmpty { "N/A" },
            studentId = profile.studentId.ifEmpty { "N/A" },
            email = profile.email.ifEmpty { defaultEmail },
            phone = profile.phone.ifEmpty { "N/A" }
        )
    }

    private fun mapToEmergencyContact(profile: FirebaseUserProfile): EmergencyContact {
        return EmergencyContact(
            primaryContactName = profile.emergencyName1.ifEmpty { "N/A" },
            primaryContactPhone = profile.emergencyPhone1.ifEmpty { "N/A" },
            secondaryContactName = profile.emergencyName2.ifEmpty { "N/A" }
        )
    }

    private fun mapToMedicalInfo(profile: FirebaseUserProfile): MedicalInfo {
        return MedicalInfo(
            bloodType = profile.bloodType.ifEmpty { "N/A" },
            primaryPhysician = profile.doctorName.ifEmpty { "N/A" },
            physicianPhone = profile.doctorPhone.ifEmpty { "N/A" },
            insuranceProvider = profile.insuranceProvider.ifEmpty { "N/A" }
        )
    }

    private fun mapToAllergies(profile: FirebaseUserProfile): Allergies {
        return Allergies(
            foodAllergies = profile.foodAllergies.ifEmpty { "N/A" },
            environmentalAllergies = profile.environmentalAllergies.ifEmpty { "N/A" },
            drugAllergies = profile.drugAllergies.ifEmpty { "N/A" },
            severityNotes = profile.severityNotes.ifEmpty { "N/A" }
        )
    }

    private fun mapToMedications(profile: FirebaseUserProfile): Medications {
        return Medications(
            dailyMedications = profile.dailyMedications.ifEmpty { "N/A" },
            emergencyMedications = profile.emergencyMedications.ifEmpty { "N/A" },
            vitaminsSupplements = profile.vitaminsSupplements.ifEmpty { "N/A" },
            specialInstructions = profile.specialInstructions.ifEmpty { "N/A" }
        )
    }
}

