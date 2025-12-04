package com.example.brigadist.data.local.dao

import androidx.room.*
import com.example.brigadist.data.local.entity.EmergencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyDao {
    @Query("SELECT * FROM emergencies WHERE status IN (:statuses) ORDER BY createdAt DESC")
    fun getEmergenciesByStatus(statuses: List<String>): Flow<List<EmergencyEntity>>
    
    @Query("SELECT * FROM emergencies WHERE status IN (:statuses) ORDER BY createdAt DESC")
    suspend fun getEmergenciesByStatusSync(statuses: List<String>): List<EmergencyEntity>
    
    @Query("SELECT * FROM emergencies WHERE emergencyKey = :key LIMIT 1")
    suspend fun getEmergencyByKey(key: String): EmergencyEntity?
    
    @Query("SELECT * FROM emergencies WHERE assignedBrigadistId = :brigadistEmail AND status = :status")
    suspend fun getEmergenciesByBrigadist(brigadistEmail: String, status: String): List<EmergencyEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergency(emergency: EmergencyEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEmergencies(emergencies: List<EmergencyEntity>)
    
    @Update
    suspend fun updateEmergency(emergency: EmergencyEntity)
    
    @Query("UPDATE emergencies SET status = :newStatus, assignedBrigadistId = :brigadistEmail, updatedAt = :updatedAt WHERE emergencyKey = :key")
    suspend fun updateEmergencyStatus(key: String, newStatus: String, brigadistEmail: String, updatedAt: Long)
    
    @Query("DELETE FROM emergencies WHERE emergencyKey = :key")
    suspend fun deleteEmergency(key: String)
    
    @Query("DELETE FROM emergencies")
    suspend fun deleteAllEmergencies()
}

