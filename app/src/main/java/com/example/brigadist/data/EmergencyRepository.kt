package com.example.brigadist.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.brigadist.ui.sos.model.Emergency
import com.google.firebase.database.FirebaseDatabase

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
     * With Firebase offline persistence enabled, writes will be queued if offline
     * and automatically synced when connection is restored.
     * 
     * @param emergency The emergency object to create (emergencyID will be generated if not set)
     * @param onSuccess Callback invoked when emergency write is queued/sent
     * @param onError Callback invoked with error message if creation fails
     * @param onOffline Callback invoked when device is offline (optional)
     */
    fun createEmergency(
        emergency: Emergency,
        onSuccess: () -> Unit,
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
            database.push().setValue(emergencyWithId)
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
        database.push().setValue(emergencyWithId)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error al crear la emergencia")
            }
    }
}

