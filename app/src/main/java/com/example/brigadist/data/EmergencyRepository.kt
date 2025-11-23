package com.example.brigadist.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.brigadist.cache.EmergencyCacheManager
import com.example.brigadist.ui.sos.model.Emergency
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EmergencyRepository(private val context: Context? = null) {
    private val database = FirebaseDatabase.getInstance().getReference("Emergency")
    private val emergencyCache = context?.let { EmergencyCacheManager.getInstance(it) }

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
     * Uses custom EmergencyCacheManager for offline storage and sync.
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
        // Generate unique emergencyID if not already set
        val emergencyWithId = if (emergency.emergencyID == 0L) {
            val pushKey = database.push().key
            val generatedId = pushKey?.takeLast(8)?.toLongOrNull() ?: System.currentTimeMillis() % 100000000
            emergency.copy(emergencyID = generatedId)
        } else {
            emergency
        }

        // Check if offline
        if (!isOnline()) {
            // Cache the emergency for later sync
            if (emergencyCache != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    emergencyCache.cacheEmergency(emergencyWithId)
                }
            }
            // Also try Firebase persistence as backup
            val pushRef = database.push()
            pushRef.setValue(emergencyWithId)
            onOffline?.invoke()
            return
        }

        // Try to write directly to Firebase
        val pushRef = database.push()
        val pushKey = pushRef.key ?: ""
        pushRef.setValue(emergencyWithId)
            .addOnSuccessListener {
                onSuccess(pushKey)
            }
            .addOnFailureListener { exception ->
                // If Firebase write fails, cache it for retry
                if (emergencyCache != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        emergencyCache.cacheEmergency(emergencyWithId)
                    }
                }
                onError(exception.message ?: "Error al crear la emergencia")
            }
    }
    
    /**
     * Sync all pending emergencies from cache
     */
    fun syncPendingEmergencies(
        onComplete: (successCount: Int, failureCount: Int, totalCount: Int) -> Unit
    ) {
        if (emergencyCache == null) {
            onComplete(0, 0, 0)
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = emergencyCache.syncPendingEmergencies()
            onComplete(result.successCount, result.failureCount, result.totalCount)
        }
    }
    
    /**
     * Check if there are pending emergencies
     */
    fun hasPendingEmergencies(): Boolean {
        return emergencyCache?.hasPendingEmergencies() ?: false
    }

    /**
     * Updates ChatUsed field on a specific emergency by push key.
     * Also removes any lowercase 'chatUsed' field that might exist.
     */
    fun updateChatUsed(emergencyKey: String, onComplete: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val updates = mapOf(
            "ChatUsed" to true,
            "chatUsed" to null  // Remove lowercase field if it exists
        )
        database.child(emergencyKey).updateChildren(updates)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { ex -> onError(ex.message ?: "Failed to update ChatUsed") }
    }
}

