package com.example.brigadist.cache

import android.content.Context
import android.util.Log
import com.example.brigadist.ui.sos.model.Emergency
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Custom emergency cache manager for offline emergency storage and sync
 * Stores emergency data locally and syncs when connection is restored
 */
class EmergencyCacheManager private constructor(context: Context) {
    
    private val cacheDir: File
    private val metadataFile: File
    private val syncLock = Any()
    
    data class CachedEmergency(
        val id: String,
        val emergency: Emergency,
        val status: SyncStatus,
        val createdAt: Long,
        val lastSyncAttempt: Long = 0,
        val retryCount: Int = 0
    )
    
    enum class SyncStatus {
        PENDING,      // Waiting to be synced
        SYNCING,      // Currently being synced
        SYNCED,       // Successfully synced
        FAILED        // Sync failed (will retry)
    }
    
    init {
        cacheDir = File(context.cacheDir, "emergency_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        metadataFile = File(cacheDir, "metadata.json")
        loadMetadata()
    }
    
    companion object {
        private const val TAG = "EmergencyCacheManager"
        private const val MAX_RETRY_COUNT = 3
        
        @Volatile
        private var INSTANCE: EmergencyCacheManager? = null
        
        fun getInstance(context: Context): EmergencyCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EmergencyCacheManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val cachedEmergencies = mutableMapOf<String, CachedEmergency>()
    
    /**
     * Cache an emergency for later sync
     */
    suspend fun cacheEmergency(emergency: Emergency): String {
        return withContext(Dispatchers.IO) {
            val id = UUID.randomUUID().toString()
            val cached = CachedEmergency(
                id = id,
                emergency = emergency,
                status = SyncStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )
            
            synchronized(syncLock) {
                cachedEmergencies[id] = cached
                saveEmergencyToFile(cached)
                saveMetadata()
            }
            
            Log.d(TAG, "Cached emergency: $id (${emergency.emerType})")
            id
        }
    }
    
    /**
     * Get all pending emergencies
     */
    suspend fun getPendingEmergencies(): List<CachedEmergency> {
        return withContext(Dispatchers.IO) {
            synchronized(syncLock) {
                cachedEmergencies.values.filter { 
                    it.status == SyncStatus.PENDING || it.status == SyncStatus.FAILED 
                }.sortedBy { it.createdAt }
            }
        }
    }
    
    /**
     * Check if there are pending emergencies
     */
    fun hasPendingEmergencies(): Boolean {
        synchronized(syncLock) {
            return cachedEmergencies.values.any { 
                it.status == SyncStatus.PENDING || it.status == SyncStatus.FAILED 
            }
        }
    }
    
    /**
     * Sync all pending emergencies to Firebase
     */
    suspend fun syncPendingEmergencies(): SyncResult {
        return withContext(Dispatchers.IO) {
            val pending = getPendingEmergencies()
            if (pending.isEmpty()) {
                return@withContext SyncResult(successCount = 0, failureCount = 0, totalCount = 0)
            }
            
            Log.d(TAG, "Syncing ${pending.size} pending emergencies")
            
            var successCount = 0
            var failureCount = 0
            
            for (cached in pending) {
                try {
                    // Mark as syncing
                    updateStatus(cached.id, SyncStatus.SYNCING)
                    
                    // Sync to Firebase
                    val success = syncEmergencyToFirebase(cached.emergency)
                    
                    if (success) {
                        // Mark as synced and delete
                        synchronized(syncLock) {
                            cachedEmergencies.remove(cached.id)
                            deleteEmergencyFile(cached.id)
                            saveMetadata()
                        }
                        successCount++
                        Log.d(TAG, "Successfully synced emergency: ${cached.id}")
                    } else {
                        // Mark as failed and increment retry count
                        val newRetryCount = cached.retryCount + 1
                        if (newRetryCount >= MAX_RETRY_COUNT) {
                            // Max retries reached, keep as failed but don't retry again
                            updateStatus(cached.id, SyncStatus.FAILED, newRetryCount)
                            Log.w(TAG, "Max retries reached for emergency: ${cached.id}")
                        } else {
                            updateStatus(cached.id, SyncStatus.FAILED, newRetryCount)
                            Log.d(TAG, "Sync failed for emergency: ${cached.id}, retry count: $newRetryCount")
                        }
                        failureCount++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing emergency: ${cached.id}", e)
                    val newRetryCount = cached.retryCount + 1
                    updateStatus(cached.id, SyncStatus.FAILED, newRetryCount)
                    failureCount++
                }
            }
            
            SyncResult(successCount, failureCount, pending.size)
        }
    }
    
    /**
     * Sync a single emergency to Firebase
     */
    private suspend fun syncEmergencyToFirebase(emergency: Emergency): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val database = FirebaseDatabase.getInstance().getReference("Emergency")
                
                // Generate unique emergencyID if not already set
                val emergencyWithId = if (emergency.emergencyID == 0L) {
                    val pushKey = database.push().key
                    val generatedId = pushKey?.takeLast(8)?.toLongOrNull() 
                        ?: System.currentTimeMillis() % 100000000
                    emergency.copy(emergencyID = generatedId)
                } else {
                    emergency
                }
                
                // Write to Firebase
                val pushRef = database.push()
                val pushKey = pushRef.key ?: return@withContext false
                
                // Use suspendCancellableCoroutine for async Firebase operation
                kotlinx.coroutines.suspendCancellableCoroutine<Boolean> { continuation ->
                    pushRef.setValue(emergencyWithId)
                        .addOnSuccessListener {
                            continuation.resume(true, null)
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Firebase sync failed", exception)
                            continuation.resume(false, null)
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in syncEmergencyToFirebase", e)
                false
            }
        }
    }
    
    /**
     * Update status of a cached emergency
     */
    private fun updateStatus(id: String, status: SyncStatus, retryCount: Int? = null) {
        synchronized(syncLock) {
            val cached = cachedEmergencies[id] ?: return
            val updated = cached.copy(
                status = status,
                lastSyncAttempt = System.currentTimeMillis(),
                retryCount = retryCount ?: cached.retryCount
            )
            cachedEmergencies[id] = updated
            saveEmergencyToFile(updated)
            saveMetadata()
        }
    }
    
    /**
     * Clear all cached emergencies
     */
    fun clearCache() {
        synchronized(syncLock) {
            cacheDir.listFiles()?.forEach { file ->
                if (file.name != "metadata.json") {
                    file.delete()
                }
            }
            cachedEmergencies.clear()
            metadataFile.delete()
        }
    }
    
    /**
     * Delete a specific cached emergency
     */
    fun deleteEmergency(id: String) {
        synchronized(syncLock) {
            cachedEmergencies.remove(id)
            deleteEmergencyFile(id)
            saveMetadata()
        }
    }
    
    /**
     * Save emergency to file
     */
    private fun saveEmergencyToFile(cached: CachedEmergency) {
        try {
            val file = File(cacheDir, "${cached.id}.json")
            val json = emergencyToJson(cached)
            FileOutputStream(file).use { output ->
                output.write(json.toByteArray())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving emergency to file: ${cached.id}", e)
        }
    }
    
    /**
     * Delete emergency file
     */
    private fun deleteEmergencyFile(id: String) {
        try {
            val file = File(cacheDir, "$id.json")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting emergency file: $id", e)
        }
    }
    
    /**
     * Load metadata from disk
     */
    private fun loadMetadata() {
        try {
            if (!metadataFile.exists()) return
            
            FileInputStream(metadataFile).use { input ->
                val jsonString = input.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                val emergenciesArray = json.getJSONArray("emergencies")
                
                synchronized(syncLock) {
                    for (i in 0 until emergenciesArray.length()) {
                        val id = emergenciesArray.getString(i)
                        loadEmergencyFromFile(id)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading metadata", e)
        }
    }
    
    /**
     * Load emergency from file
     */
    private fun loadEmergencyFromFile(id: String) {
        try {
            val file = File(cacheDir, "$id.json")
            if (!file.exists()) return
            
            FileInputStream(file).use { input ->
                val jsonString = input.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                
                val emergency = jsonToEmergency(json.getJSONObject("emergency"))
                val status = SyncStatus.valueOf(json.getString("status"))
                val createdAt = json.getLong("createdAt")
                val lastSyncAttempt = json.optLong("lastSyncAttempt", 0)
                val retryCount = json.optInt("retryCount", 0)
                
                cachedEmergencies[id] = CachedEmergency(
                    id = id,
                    emergency = emergency,
                    status = status,
                    createdAt = createdAt,
                    lastSyncAttempt = lastSyncAttempt,
                    retryCount = retryCount
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading emergency from file: $id", e)
        }
    }
    
    /**
     * Save metadata to disk
     */
    private fun saveMetadata() {
        try {
            val json = JSONObject()
            val emergenciesArray = org.json.JSONArray()
            cachedEmergencies.keys.forEach { emergenciesArray.put(it) }
            json.put("emergencies", emergenciesArray)
            
            FileOutputStream(metadataFile).use { output ->
                output.write(json.toString().toByteArray())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving metadata", e)
        }
    }
    
    /**
     * Convert Emergency to JSON
     */
    private fun emergencyToJson(cached: CachedEmergency): String {
        val json = JSONObject()
        json.put("id", cached.id)
        json.put("status", cached.status.name)
        json.put("createdAt", cached.createdAt)
        json.put("lastSyncAttempt", cached.lastSyncAttempt)
        json.put("retryCount", cached.retryCount)
        
        val emergencyJson = JSONObject()
        emergencyJson.put("EmerResquestTime", cached.emergency.EmerResquestTime)
        emergencyJson.put("assignedBrigadistId", cached.emergency.assignedBrigadistId)
        emergencyJson.put("createdAt", cached.emergency.createdAt)
        emergencyJson.put("date_time", cached.emergency.date_time)
        emergencyJson.put("emerType", cached.emergency.emerType)
        emergencyJson.put("emergencyID", cached.emergency.emergencyID)
        emergencyJson.put("location", cached.emergency.location)
        emergencyJson.put("secondsResponse", cached.emergency.secondsResponse)
        emergencyJson.put("seconds_response", cached.emergency.seconds_response)
        emergencyJson.put("updatedAt", cached.emergency.updatedAt)
        emergencyJson.put("userId", cached.emergency.userId)
        emergencyJson.put("ChatUsed", cached.emergency.ChatUsed)
        
        json.put("emergency", emergencyJson)
        return json.toString()
    }
    
    /**
     * Convert JSON to Emergency
     */
    private fun jsonToEmergency(json: JSONObject): Emergency {
        return Emergency(
            EmerResquestTime = json.optLong("EmerResquestTime", 0),
            assignedBrigadistId = json.optString("assignedBrigadistId", ""),
            createdAt = json.optLong("createdAt", 0),
            date_time = json.optString("date_time", ""),
            emerType = json.optString("emerType", ""),
            emergencyID = json.optLong("emergencyID", 0),
            location = json.optString("location", ""),
            secondsResponse = json.optInt("secondsResponse", 5),
            seconds_response = json.optInt("seconds_response", 5),
            updatedAt = json.optLong("updatedAt", 0),
            userId = json.optString("userId", ""),
            ChatUsed = json.optBoolean("ChatUsed", false)
        )
    }
    
    /**
     * Result of sync operation
     */
    data class SyncResult(
        val successCount: Int,
        val failureCount: Int,
        val totalCount: Int
    )
}

