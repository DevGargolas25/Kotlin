package com.example.brigadist.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import com.example.brigadist.ui.sos.model.Emergency
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class EmergencyRepository(private val context: Context? = null) {
    private val database = FirebaseDatabase.getInstance().getReference("Emergency")

    /**
     * Checks if device is currently online.
     */
    private fun isOnline(): Boolean {
        if (context == null) return true // Assume online if context not provided
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Creates a new emergency record in Firebase.
     * Logs event for BQ1: How many alerts have been sent each month in the current year?
     */
    fun createEmergency(
        emergency: Emergency,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onOffline: (() -> Unit)? = null
    ) {
        if (!isOnline()) {
            val emergencyWithId = if (emergency.emergencyID == 0L) {
                val pushKey = database.push().key
                val generatedId = pushKey?.takeLast(8)?.toLongOrNull() ?: System.currentTimeMillis() % 100000000
                emergency.copy(emergencyID = generatedId)
            } else {
                emergency
            }
            val pushRef = database.push()
            val pushKey = pushRef.key ?: ""
            pushRef.setValue(emergencyWithId)
            onOffline?.invoke()
            return
        }

        val emergencyWithId = if (emergency.emergencyID == 0L) {
            val pushKey = database.push().key
            val generatedId = pushKey?.takeLast(8)?.toLongOrNull() ?: System.currentTimeMillis() % 100000000
            emergency.copy(emergencyID = generatedId)
        } else {
            emergency
        }

        val pushRef = database.push()
        val pushKey = pushRef.key ?: ""
        pushRef.setValue(emergencyWithId)
            .addOnSuccessListener {
                logAlertSent()
                onSuccess(pushKey)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al crear la emergencia")
            }
    }

    /**
     * Logs an alert_sent event to Firebase Analytics.
     *
     */
    private fun logAlertSent() {
        val analytics = Firebase.analytics
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentMonth = dateFormat.format(Date())

        val bundle = Bundle().apply {
            putString("alert_type", "emergency_created")
            putLong("alert_sent_time", System.currentTimeMillis())
            putString("month", currentMonth)
            putString("device_model", Build.MODEL)
            putString("os_version", Build.VERSION.RELEASE)
        }

        analytics.logEvent("alert_sent", bundle)
    }

    /**
     * Updates ChatUsed field on a specific emergency by push key.
     */
    fun updateChatUsed(emergencyKey: String, onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val updates = mapOf(
            "ChatUsed" to true,
            "chatUsed" to null
        )
        database.child(emergencyKey).updateChildren(updates)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { ex -> onError(ex.message ?: "Failed to update ChatUsed") }
    }
}
