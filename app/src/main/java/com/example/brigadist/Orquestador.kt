package com.example.brigadist

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.brigadist.auth.User
import com.example.brigadist.ui.chat.model.ConversationUi
import com.example.brigadist.ui.profile.model.Allergies
import com.example.brigadist.ui.profile.model.EmergencyContact
import com.example.brigadist.ui.profile.model.FirebaseUserProfile
import com.example.brigadist.ui.profile.model.MedicalInfo
import com.example.brigadist.ui.profile.model.Medications
import com.example.brigadist.ui.profile.model.UserProfile
import com.example.brigadist.ui.theme.ThemeController
import com.example.brigadist.ui.videos.model.VideoUi
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Orquestador(
    private val user: User,
    private val context: Context
) {
    private val defaultLocation = LatLng(4.6018, -74.0661)
    private val database: FirebaseDatabase = Firebase.database
    private val userRef = database.getReference("User").child(user.id.substringAfter("|"))

    var firebaseUserProfile by mutableStateOf<FirebaseUserProfile?>(null)
        private set

    val themeController = ThemeController(context)

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                firebaseUserProfile = snapshot.getValue(FirebaseUserProfile::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun updateUserProfile(profile: FirebaseUserProfile) {
        userRef.setValue(profile)
    }

    fun getConversations(): List<ConversationUi> {
        return listOf(
            ConversationUi(1, "Brigade Assistant",
                "The main assembly points are: Main Campus: Front park…", "10:32 AM", 2),
            ConversationUi(2, "Brigade Team",
                "Meeting tonight at 7 PM in room 203. Please confirm y…", "9:45 AM", 0),
            ConversationUi(3, "Brigade Alerts",
                "Weather alert: Strong winds expected this afternoon. St…", "Yesterday", 1),
        )
    }

    fun getUserProfile(): UserProfile {
        return UserProfile(
            fullName = firebaseUserProfile?.fullName ?: user.name,
            studentId = firebaseUserProfile?.studentId ?: "",
            email = firebaseUserProfile?.email ?: user.email,
            phone = firebaseUserProfile?.phone ?: ""
        )
    }

    fun getEmergencyContact(): EmergencyContact {
        return EmergencyContact(
            primaryContactName = firebaseUserProfile?.emergencyName1 ?: "",
            primaryContactPhone = firebaseUserProfile?.emergencyPhone1 ?: "",
            secondaryContactName = firebaseUserProfile?.emergencyName2 ?: ""
        )
    }

    fun getMedicalInfo(): MedicalInfo {
        return MedicalInfo(
            bloodType = firebaseUserProfile?.bloodType ?: "",
            primaryPhysician = firebaseUserProfile?.doctorName ?: "",
            physicianPhone = firebaseUserProfile?.doctorPhone ?: "",
            insuranceProvider = firebaseUserProfile?.insuranceProvider ?: ""
        )
    }

    fun getAllergies(): Allergies {
        return Allergies(
            foodAllergies = firebaseUserProfile?.foodAllergies ?: "",
            environmentalAllergies = firebaseUserProfile?.environmentalAllergies ?: "",
            drugAllergies = firebaseUserProfile?.drugAllergies ?: "",
            severityNotes = firebaseUserProfile?.severityNotes ?: ""
        )
    }

    fun getMedications(): Medications {
        return Medications(
            dailyMedications = firebaseUserProfile?.dailyMedications ?: "",
            emergencyMedications = firebaseUserProfile?.emergencyMedications ?: "",
            vitaminsSupplements = firebaseUserProfile?.vitaminsSupplements ?: "",
            specialInstructions = firebaseUserProfile?.specialInstructions ?: ""
        )
    }

    fun getVideos(): List<VideoUi> {
        return listOf(
            VideoUi(id = "1", title = "Fire Safety Basics", durationSec = 755, tags = listOf("Fire Safety"), author = "Student Brigade", viewsText = "1.2k views", ageText = "1 week ago", description = "Basic fire safety measures and evacuation procedures."),
            VideoUi(id = "2", title = "First-Aid for Burns", durationSec = 920, tags = listOf("Medical"), author = "Student Brigade", viewsText = "2.5k views", ageText = "2 weeks ago", description = "How to administer first-aid for different types of burns."),
            VideoUi(id = "3", title = "Using a Fire Extinguisher", durationSec = 525, tags = listOf("Fire Safety"), author = "Student Brigade", viewsText = "890 views", ageText = "3 weeks ago", description = "A step-by-step guide on how to operate a fire extinguisher."),
            VideoUi(id = "4", title = "CPR Techniques", durationSec = 1200, tags = listOf("Medical"), author = "Student Brigade", viewsText = "3.1k views", ageText = "1 month ago", description = "Learn the latest CPR techniques for adults, children, and infants."),
            VideoUi(id = "5", title = "Earthquake Preparedness", durationSec = 970, tags = listOf("Natural Disasters"), author = "Student Brigade", viewsText = "1.5k views", ageText = "2 months ago", description = "What to do before, during, and after an earthquake.")
        )
    }

    fun getVideoCategories(): List<String> {
        return listOf("All", "Fire Safety", "Medical", "Natural Disasters")
    }

    fun getDefaultCameraPosition(): CameraPosition {
        return CameraPosition.Builder()
            .target(defaultLocation)
            .zoom(15f)
            .build()
    }

    fun getDefaultLocation(): LatLng {
        return defaultLocation
    }
}
