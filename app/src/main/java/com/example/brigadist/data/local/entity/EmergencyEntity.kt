package com.example.brigadist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.brigadist.ui.sos.model.Emergency

@Entity(tableName = "emergencies")
data class EmergencyEntity(
    @PrimaryKey
    val emergencyKey: String,  // Firebase key
    val emergencyID: Long,
    val emerType: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val userId: String,
    val assignedBrigadistId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val date_time: String,
    val EmerResquestTime: Long,
    val secondsResponse: Int,
    val ChatUsed: Boolean
) {
    fun toEmergency(): Emergency {
        return Emergency(
            emergencyID = emergencyID,
            emerType = emerType,
            location = location,
            latitude = latitude,
            longitude = longitude,
            status = status,
            userId = userId,
            assignedBrigadistId = assignedBrigadistId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            date_time = date_time,
            EmerResquestTime = EmerResquestTime,
            secondsResponse = secondsResponse,
            seconds_response = secondsResponse,
            ChatUsed = ChatUsed
        )
    }
    
    companion object {
        fun fromEmergency(key: String, emergency: Emergency): EmergencyEntity {
            return EmergencyEntity(
                emergencyKey = key,
                emergencyID = emergency.emergencyID,
                emerType = emergency.emerType,
                location = emergency.location,
                latitude = emergency.latitude,
                longitude = emergency.longitude,
                status = emergency.status,
                userId = emergency.userId,
                assignedBrigadistId = emergency.assignedBrigadistId,
                createdAt = emergency.createdAt,
                updatedAt = emergency.updatedAt,
                date_time = emergency.date_time,
                EmerResquestTime = emergency.EmerResquestTime,
                secondsResponse = emergency.secondsResponse,
                ChatUsed = emergency.ChatUsed
            )
        }
    }
}

