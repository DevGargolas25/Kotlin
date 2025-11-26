package com.example.brigadist.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.brigadist.ui.profile.model.FirebaseUserProfile
import com.example.brigadist.ui.sos.model.Emergency
import org.json.JSONObject

class EmergencyPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "emergency_preferences",
        Context.MODE_PRIVATE
    )
    
    private val keySelectedEmergencyKey = "selected_emergency_key"
    private val keySelectedEmergencyData = "selected_emergency_data"
    private val keyUserProfileData = "user_profile_data"
    private val keyBrigadistEmail = "brigadist_email"
    private val keyActiveMedicalEmergencyKey = "active_medical_emergency_key"
    
    /**
     * Save the selected emergency and its data locally
     */
    fun saveSelectedEmergency(
        emergencyKey: String,
        emergency: Emergency,
        userProfile: FirebaseUserProfile?,
        brigadistEmail: String
    ) {
        val emergencyJson = emergencyToJson(emergency)
        val userProfileJson = userProfile?.let { userProfileToJson(it) }
        
        prefs.edit().apply {
            putString(keySelectedEmergencyKey, emergencyKey)
            putString(keySelectedEmergencyData, emergencyJson)
            putString(keyUserProfileData, userProfileJson)
            putString(keyBrigadistEmail, brigadistEmail)
            apply()
        }
    }
    
    /**
     * Load the saved emergency key
     */
    fun getSelectedEmergencyKey(): String? {
        return prefs.getString(keySelectedEmergencyKey, null)
    }
    
    /**
     * Load the saved emergency data
     */
    fun getSelectedEmergency(): Emergency? {
        val jsonString = prefs.getString(keySelectedEmergencyData, null) ?: return null
        return try {
            jsonToEmergency(JSONObject(jsonString))
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Load the saved user profile data
     */
    fun getUserProfile(): FirebaseUserProfile? {
        val jsonString = prefs.getString(keyUserProfileData, null) ?: return null
        return try {
            jsonToUserProfile(JSONObject(jsonString))
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get the brigadist email who selected this emergency
     */
    fun getBrigadistEmail(): String? {
        return prefs.getString(keyBrigadistEmail, null)
    }
    
    /**
     * Clear all saved emergency data
     */
    fun clearSelectedEmergency() {
        prefs.edit().apply {
            remove(keySelectedEmergencyKey)
            remove(keySelectedEmergencyData)
            remove(keyUserProfileData)
            remove(keyBrigadistEmail)
            apply()
        }
    }
    
    /**
     * Check if there's a saved emergency
     */
    fun hasSelectedEmergency(): Boolean {
        return getSelectedEmergencyKey() != null
    }
    
    /**
     * Save active medical emergency key (for Contact Brigade screen persistence)
     */
    fun saveActiveMedicalEmergency(emergencyKey: String) {
        prefs.edit().putString(keyActiveMedicalEmergencyKey, emergencyKey).apply()
    }
    
    /**
     * Get active medical emergency key
     */
    fun getActiveMedicalEmergencyKey(): String? {
        return prefs.getString(keyActiveMedicalEmergencyKey, null)
    }
    
    /**
     * Clear active medical emergency
     */
    fun clearActiveMedicalEmergency() {
        prefs.edit().remove(keyActiveMedicalEmergencyKey).apply()
    }
    
    /**
     * Check if there's an active medical emergency
     */
    fun hasActiveMedicalEmergency(): Boolean {
        return getActiveMedicalEmergencyKey() != null
    }
    
    // Emergency serialization
    private fun emergencyToJson(emergency: Emergency): String {
        val json = JSONObject()
        json.put("EmerResquestTime", emergency.EmerResquestTime)
        json.put("assignedBrigadistId", emergency.assignedBrigadistId)
        json.put("createdAt", emergency.createdAt)
        json.put("date_time", emergency.date_time)
        json.put("emerType", emergency.emerType)
        json.put("emergencyID", emergency.emergencyID)
        json.put("location", emergency.location)
        json.put("latitude", emergency.latitude)
        json.put("longitude", emergency.longitude)
        json.put("status", emergency.status)
        json.put("secondsResponse", emergency.secondsResponse)
        json.put("seconds_response", emergency.seconds_response)
        json.put("updatedAt", emergency.updatedAt)
        json.put("userId", emergency.userId)
        json.put("ChatUsed", emergency.ChatUsed)
        return json.toString()
    }
    
    private fun jsonToEmergency(json: JSONObject): Emergency {
        return Emergency(
            EmerResquestTime = json.optLong("EmerResquestTime", 0),
            assignedBrigadistId = json.optString("assignedBrigadistId", ""),
            createdAt = json.optLong("createdAt", 0),
            date_time = json.optString("date_time", ""),
            emerType = json.optString("emerType", ""),
            emergencyID = json.optLong("emergencyID", 0),
            location = json.optString("location", ""),
            latitude = json.optDouble("latitude", 0.0),
            longitude = json.optDouble("longitude", 0.0),
            status = json.optString("status", "Unattended"),
            secondsResponse = json.optInt("secondsResponse", 5),
            seconds_response = json.optInt("seconds_response", 5),
            updatedAt = json.optLong("updatedAt", 0),
            userId = json.optString("userId", ""),
            ChatUsed = json.optBoolean("ChatUsed", false)
        )
    }
    
    // UserProfile serialization
    private fun userProfileToJson(profile: FirebaseUserProfile): String {
        val json = JSONObject()
        json.put("bloodType", profile.bloodType)
        json.put("dailyMedications", profile.dailyMedications)
        json.put("doctorName", profile.doctorName)
        json.put("doctorPhone", profile.doctorPhone)
        json.put("drugAllergies", profile.drugAllergies)
        json.put("email", profile.email)
        json.put("emergencyMedications", profile.emergencyMedications)
        json.put("emergencyName1", profile.emergencyName1)
        json.put("emergencyName2", profile.emergencyName2)
        json.put("emergencyPhone1", profile.emergencyPhone1)
        json.put("emergencyPhone2", profile.emergencyPhone2)
        json.put("environmentalAllergies", profile.environmentalAllergies)
        json.put("foodAllergies", profile.foodAllergies)
        json.put("fullName", profile.fullName)
        json.put("insuranceProvider", profile.insuranceProvider)
        json.put("phone", profile.phone)
        json.put("severityNotes", profile.severityNotes)
        json.put("specialInstructions", profile.specialInstructions)
        json.put("studentId", profile.studentId)
        json.put("userType", profile.userType)
        json.put("vitaminsMedications", profile.vitaminsMedications)
        json.put("vitaminsSupplements", profile.vitaminsSupplements)
        return json.toString()
    }
    
    private fun jsonToUserProfile(json: JSONObject): FirebaseUserProfile {
        return FirebaseUserProfile(
            bloodType = json.optString("bloodType", ""),
            dailyMedications = json.optString("dailyMedications", ""),
            doctorName = json.optString("doctorName", ""),
            doctorPhone = json.optString("doctorPhone", ""),
            drugAllergies = json.optString("drugAllergies", ""),
            email = json.optString("email", ""),
            emergencyMedications = json.optString("emergencyMedications", ""),
            emergencyName1 = json.optString("emergencyName1", ""),
            emergencyName2 = json.optString("emergencyName2", ""),
            emergencyPhone1 = json.optString("emergencyPhone1", ""),
            emergencyPhone2 = json.optString("emergencyPhone2", ""),
            environmentalAllergies = json.optString("environmentalAllergies", ""),
            foodAllergies = json.optString("foodAllergies", ""),
            fullName = json.optString("fullName", ""),
            insuranceProvider = json.optString("insuranceProvider", ""),
            phone = json.optString("phone", ""),
            severityNotes = json.optString("severityNotes", ""),
            specialInstructions = json.optString("specialInstructions", ""),
            studentId = json.optString("studentId", ""),
            userType = json.optString("userType", ""),
            vitaminsMedications = json.optString("vitaminsMedications", ""),
            vitaminsSupplements = json.optString("vitaminsSupplements", "")
        )
    }
}

