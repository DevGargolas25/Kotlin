package com.example.brigadist.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.brigadist.cache.EmergencyCacheManager
import com.example.brigadist.data.local.EmergencyDatabase
import com.example.brigadist.data.local.entity.EmergencyEntity
import com.example.brigadist.ui.sos.model.Emergency
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmergencyRepository(private val context: Context? = null) {
    private val database = FirebaseDatabase.getInstance().getReference("Emergency")
    private val emergencyCache = context?.let { EmergencyCacheManager.getInstance(it) }
    private val localDatabase = context?.let { EmergencyDatabase.getDatabase(it) }
    private val localDao = localDatabase?.emergencyDao()

    /**
     * Checks if device is currently online.
     */
    fun isOnline(): Boolean {
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
                // Explicitly switch to Main dispatcher for UI callback
                CoroutineScope(Dispatchers.Main).launch {
                    onSuccess(pushKey)
                }
            }
            .addOnFailureListener { exception ->
                // If Firebase write fails, cache it for retry
                if (emergencyCache != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        emergencyCache.cacheEmergency(emergencyWithId)
                    }
                }
                // Explicitly switch to Main dispatcher for UI callback
                CoroutineScope(Dispatchers.Main).launch {
                    onError(exception.message ?: "Error al crear la emergencia")
                }
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
            // Explicitly switch to Main dispatcher for UI callback
            withContext(Dispatchers.Main) {
                onComplete(result.successCount, result.failureCount, result.totalCount)
            }
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
            .addOnSuccessListener {
                // Explicitly switch to Main dispatcher for UI callback
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete()
                }
            }
            .addOnFailureListener { ex ->
                // Explicitly switch to Main dispatcher for UI callback
                CoroutineScope(Dispatchers.Main).launch {
                    onError(ex.message ?: "Failed to update ChatUsed")
                }
            }
    }

    /**
     * Listen to emergencies with specific status(es)
     * @param statuses List of statuses to filter (e.g., ["Unattended"] or ["Unattended", "In progress"])
     * @param onEmergencyUpdate Callback invoked when emergencies are updated
     * @return ValueEventListener that can be used to remove the listener
     */
    fun listenToEmergenciesByStatus(
        statuses: List<String>,
        onEmergencyUpdate: (List<Pair<String, Emergency>>) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Process data on IO dispatcher
                CoroutineScope(Dispatchers.IO).launch {
                    val emergencies = mutableListOf<Pair<String, Emergency>>()
                    for (child in snapshot.children) {
                        val emergency = child.getValue(Emergency::class.java)
                        val key = child.key ?: continue
                        if (emergency != null && emergency.status in statuses) {
                            emergencies.add(Pair(key, emergency))
                            
                            // Save to Room database for offline access
                            try {
                                val entity = EmergencyEntity.fromEmergency(key, emergency)
                                localDao?.insertEmergency(entity)
                            } catch (e: Exception) {
                                // Log error but don't fail the operation
                            }
                        }
                    }
                    // Switch to Main dispatcher for UI callback
                    withContext(Dispatchers.Main) {
                        onEmergencyUpdate(emergencies)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Fallback to local database when Firebase fails
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val localEmergencies = localDao?.getEmergenciesByStatusSync(statuses)
                            ?.map { Pair(it.emergencyKey, it.toEmergency()) }
                            ?: emptyList()
                        withContext(Dispatchers.Main) {
                            onEmergencyUpdate(localEmergencies)
                        }
                    } catch (e: Exception) {
                        // If local DB also fails, return empty list
                        withContext(Dispatchers.Main) {
                            onEmergencyUpdate(emptyList())
                        }
                    }
                }
            }
        }
        database.addValueEventListener(listener)
        return listener
    }

    /**
     * Update emergency status
     * @param emergencyKey The Firebase key of the emergency
     * @param newStatus The new status (e.g., "In progress")
     * @param brigadistEmail The email of the brigadist attending the emergency
     * @param onSuccess Callback on success
     * @param onError Callback on error
     */
    fun updateEmergencyStatus(
        emergencyKey: String,
        newStatus: String,
        brigadistEmail: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val updatedAt = System.currentTimeMillis()
        val updates = mapOf(
            "status" to newStatus,
            "assignedBrigadistId" to brigadistEmail,
            "updatedAt" to updatedAt
        )
        database.child(emergencyKey).updateChildren(updates)
            .addOnSuccessListener {
                // Update local database as well
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        localDao?.updateEmergencyStatus(emergencyKey, newStatus, brigadistEmail, updatedAt)
                    } catch (e: Exception) {
                        // Log error but don't fail the operation
                    }
                }
                // Explicitly switch to Main dispatcher for UI callback
                CoroutineScope(Dispatchers.Main).launch {
                    onSuccess()
                }
            }
            .addOnFailureListener { ex ->
                // Explicitly switch to Main dispatcher for UI callback
                CoroutineScope(Dispatchers.Main).launch {
                    onError(ex.message ?: "Failed to update emergency status")
                }
            }
    }
    
    /**
     * Resolve emergency (only updates status, preserves assignedBrigadistId)
     * @param emergencyKey The Firebase key of the emergency
     * @param onSuccess Callback on success
     * @param onError Callback on error
     */
    fun resolveEmergency(
        emergencyKey: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val updates = mapOf(
            "status" to "Resolved",
            "updatedAt" to System.currentTimeMillis()
        )
        database.child(emergencyKey).updateChildren(updates)
            .addOnSuccessListener {
                // Explicitly switch to Main dispatcher for UI callback
                CoroutineScope(Dispatchers.Main).launch {
                    onSuccess()
                }
            }
            .addOnFailureListener { ex ->
                // Explicitly switch to Main dispatcher for UI callback
                CoroutineScope(Dispatchers.Main).launch {
                    onError(ex.message ?: "Failed to resolve emergency")
                }
            }
    }

    /**
     * Listen to a single emergency by key
     * @param emergencyKey The Firebase key of the emergency
     * @param onEmergencyUpdate Callback invoked when emergency is updated
     * @return ValueEventListener that can be used to remove the listener
     */
    fun listenToEmergencyByKey(
        emergencyKey: String,
        onEmergencyUpdate: (Emergency?) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val emergency = snapshot.getValue(Emergency::class.java)
                
                // Save to local database if not null
                if (emergency != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val entity = EmergencyEntity.fromEmergency(emergencyKey, emergency)
                            localDao?.insertEmergency(entity)
                        } catch (e: Exception) {
                            // Log error but don't fail the operation
                        }
                    }
                }
                
                // Explicitly switch to Main dispatcher for UI callback
                CoroutineScope(Dispatchers.Main).launch {
                    onEmergencyUpdate(emergency)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error - try to get from local database as fallback
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val localEmergency = localDao?.getEmergencyByKey(emergencyKey)?.toEmergency()
                        withContext(Dispatchers.Main) {
                            onEmergencyUpdate(localEmergency)
                        }
                    } catch (e: Exception) {
                        // If local DB also fails, return null
                        withContext(Dispatchers.Main) {
                            onEmergencyUpdate(null)
                        }
                    }
                }
            }
        }
        database.child(emergencyKey).addValueEventListener(listener)
        return listener
    }
    
    /**
     * Get a single emergency by key (one-time read)
     */
    fun getEmergencyByKey(
        emergencyKey: String,
        onSuccess: (Emergency?) -> Unit,
        onError: (String) -> Unit
    ) {
        database.child(emergencyKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val emergency = snapshot.getValue(Emergency::class.java)
                // Explicitly switch to Main dispatcher for UI callback
                CoroutineScope(Dispatchers.Main).launch {
                    onSuccess(emergency)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Explicitly switch to Main dispatcher for UI callback
                CoroutineScope(Dispatchers.Main).launch {
                    onError(error.message ?: "Failed to get emergency")
                }
            }
        })
    }
    
    /**
     * Remove a listener
     */
    fun removeListener(listener: ValueEventListener) {
        database.removeEventListener(listener)
    }
    
    /**
     * Get emergencies from local database (for offline access)
     * @param statuses List of statuses to filter
     * @return List of emergency pairs (key, emergency)
     */
    suspend fun getEmergenciesFromLocal(statuses: List<String>): List<Pair<String, Emergency>> {
        return withContext(Dispatchers.IO) {
            try {
                localDao?.getEmergenciesByStatusSync(statuses)
                    ?.map { Pair(it.emergencyKey, it.toEmergency()) }
                    ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Get emergencies assigned to a specific brigadist from local database
     * @param brigadistEmail The brigadist's email
     * @param status The emergency status
     * @return List of emergency pairs (key, emergency)
     */
    suspend fun getEmergenciesByBrigadistFromLocal(
        brigadistEmail: String,
        status: String
    ): List<Pair<String, Emergency>> {
        return withContext(Dispatchers.IO) {
            try {
                localDao?.getEmergenciesByBrigadist(brigadistEmail, status)
                    ?.map { Pair(it.emergencyKey, it.toEmergency()) }
                    ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

