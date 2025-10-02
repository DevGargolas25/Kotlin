package com.example.brigadist

import com.example.brigadist.auth.User
import com.example.brigadist.ui.chat.model.ConversationUi
import com.example.brigadist.ui.profile.model.Allergies
import com.example.brigadist.ui.profile.model.EmergencyContact
import com.example.brigadist.ui.profile.model.MedicalInfo
import com.example.brigadist.ui.profile.model.Medications
import com.example.brigadist.ui.profile.model.UserProfile
import com.example.brigadist.ui.videos.model.VideoUi
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class Orquestador(private val user: User) {
    private val defaultLocation = LatLng(4.6018, -74.0661)

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
            fullName = user.name,
            studentId = "SB2024001", // This should be fetched from your backend
            email = user.email,
            phone = "+1 (555) 123-4567" // This should be fetched from your backend
        )
    }

    fun getEmergencyContact(): EmergencyContact {
        return EmergencyContact(
            primaryContactName = "Jane Smith (Mother)",
            primaryContactPhone = "+1 (555) 987-6543",
            secondaryContactName = ""
        )
    }

    fun getMedicalInfo(): MedicalInfo {
        return MedicalInfo(
            bloodType = "O+",
            primaryPhysician = "Dr. Sarah Johnson",
            physicianPhone = "+1 (555) 234-5678",
            insuranceProvider = "University Health Plan"
        )
    }

    fun getAllergies(): Allergies {
        return Allergies(
            foodAllergies = "Peanuts, Shellfish",
            environmentalAllergies = "Pollen, Dust mites",
            drugAllergies = "",
            severityNotes = "Carry EpiPen for severe reactions"
        )
    }

    fun getMedications(): Medications {
        return Medications(
            dailyMedications = "Inhaler (Albuterol) - As needed for asthma",
            emergencyMedications = "EpiPen - For severe allergic reactions",
            vitaminsSupplements = "Vitamin D3 - 1000 IU daily",
            specialInstructions = "Keep inhaler and EpiPen accessible at all times"
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