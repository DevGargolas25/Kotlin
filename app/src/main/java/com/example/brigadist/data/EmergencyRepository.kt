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
     * Checks if the device currently has an active and validated Internet connection.
     *
     * @return True if connected to the Internet, false otherwise.
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
     * With Firebase offline persistence enabled, writes will be queued if offline
     * and automatically synced when connection is restored.
     *
     * Additionally logs an Analytics event for the Business Question:
     * “How many alerts have been sent each month in the current year?”
     *
     * @param emergency The emergency object to create (emergencyID will be generated if not set)
     * @param onSuccess Callback invoked when emergency write is queued/sent, with the push key
     * @param onError Callback invoked with error message if creation fails
     * @param onOffline Callback invoked when device is offline (optional)
     */
    fun createEmergency(
        emergency: Emergency,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onOffline: (() -> Unit)? = null
    ) {
        // Check if offline
        if (!isOnline()) {
            // Firebase persistence will queue this write automatically
            // Generate unique emergencyID if not already set
            val emergencyWithId = if (emergency.emergencyID == 0L) {
                val pushKey = database.push().key
                val generatedId = pushKey?.takeLast(8)?.toLongOrNull() ?: System.currentTimeMillis() % 100000000
                emergency.copy(emergencyID = generatedId)
            } else {
                emergency
            }

            // Write to Firebase (will be queued by persistence)
            val pushRef = database.push()
            val pushKey = pushRef.key ?: ""
            pushRef.setValue(emergencyWithId)
            // Only call onOffline, not onSuccess - this prevents the confirmation modal from showing
            onOffline?.invoke()
            return
        }

        // Generate unique emergencyID if not already set
        val emergencyWithId = if (emergency.emergencyID == 0L) {
            val pushKey = database.push().key
            val generatedId = pushKey?.takeLast(8)?.toLongOrNull() ?: System.currentTimeMillis() % 100000000
            emergency.copy(emergencyID = generatedId)
        } else {
            emergency
        }

        // Write emergency to Firebase
        val pushRef = database.push()
        val pushKey = pushRef.key ?: ""
        pushRef.setValue(emergencyWithId)
            .addOnSuccessListener {
                // Log event for monthly alert tracking in BigQuery (BQ1)
                logAlertSent()
                onSuccess(pushKey)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al crear la emergencia")
            }
    }

    /**
     * Logs an alert_sent event to Firebase Analytics.
     * This event is used for the Business Question:
     * “How many alerts have been sent each month in the current year?”
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
     * Also removes any lowercase 'chatUsed' field that might exist.
     *
     * @param emergencyKey The unique push key of the emergency.
     * @param onComplete Callback invoked when the update completes successfully.
     * @param onError Callback invoked if the update fails.
     */
    fun updateChatUsed(
        emergencyKey: String,
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val updates = mapOf(
            "ChatUsed" to true,
            "chatUsed" to null  // Remove lowercase field if it exists
        )

        database.child(emergencyKey).updateChildren(updates)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { ex -> onError(ex.message ?: "Failed to update ChatUsed") }
    }
}
