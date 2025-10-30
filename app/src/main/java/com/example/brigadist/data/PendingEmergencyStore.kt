package com.example.brigadist.data

import android.content.Context
import android.content.SharedPreferences

class PendingEmergencyStore(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "pending_emergency_preferences",
        Context.MODE_PRIVATE
    )
    
    private val keyHasPendingEmergency = "has_pending_emergency"
    private val keyEmergencyData = "emergency_data"
    
    /**
     * Sets that there is a pending emergency waiting to be sent.
     */
    fun setPendingEmergency(exists: Boolean = true) {
        prefs.edit()
            .putBoolean(keyHasPendingEmergency, exists)
            .apply()
    }
    
    /**
     * Checks if there is a pending emergency.
     */
    fun hasPendingEmergency(): Boolean {
        return prefs.getBoolean(keyHasPendingEmergency, false)
    }
    
    /**
     * Clears the pending emergency flag.
     */
    fun clearPendingEmergency() {
        prefs.edit()
            .putBoolean(keyHasPendingEmergency, false)
            .remove(keyEmergencyData)
            .apply()
    }
    
    /**
     * Stores emergency data as JSON string (optional, for reference).
     */
    fun saveEmergencyData(data: String) {
        prefs.edit()
            .putString(keyEmergencyData, data)
            .apply()
    }
    
    /**
     * Retrieves stored emergency data (optional).
     */
    fun getEmergencyData(): String? {
        return prefs.getString(keyEmergencyData, null)
    }
}

