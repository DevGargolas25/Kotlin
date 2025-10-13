package com.example.brigadist.data.analytics

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("analytics_summaries")
    
    /**
     * Fetch permission changes summary
     */
    fun getPermissionChanges(): Flow<Result<PermissionChangesSummary>> = flow {
        try {
            val document = collection.document("permission_changes_last14d").get().await()
            val data = parsePermissionChangesData(document.data)
            val updatedAt = document.getLong("updatedAt") ?: 0L
            emit(Result.success(PermissionChangesSummary(data, updatedAt)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Fetch permission status summary
     */
    fun getPermissionStatus(): Flow<Result<PermissionStatusSummary>> = flow {
        try {
            val document = collection.document("permission_status_last14d").get().await()
            val data = parsePermissionStatusData(document.data)
            val updatedAt = document.getLong("updatedAt") ?: 0L
            emit(Result.success(PermissionStatusSummary(data, updatedAt)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Fetch profile updates summary
     */
    fun getProfileUpdates(): Flow<Result<ProfileUpdatesSummary>> = flow {
        try {
            val document = collection.document("profile_updates_last14d").get().await()
            val data = parseProfileUpdatesData(document.data)
            val updatedAt = document.getLong("updatedAt") ?: 0L
            emit(Result.success(ProfileUpdatesSummary(data, updatedAt)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Fetch screen views summary
     */
    fun getScreenViews(): Flow<Result<ScreenViewsSummary>> = flow {
        try {
            val document = collection.document("screen_views_last14d").get().await()
            val data = parseScreenViewsData(document.data)
            val updatedAt = document.getLong("updatedAt") ?: 0L
            emit(Result.success(ScreenViewsSummary(data, updatedAt)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    private fun parsePermissionChangesData(data: Map<String, Any>?): Map<String, LocationPermissionData> {
        if (data == null) return emptyMap()
        
        val dataMap = data["data"] as? Map<String, Any> ?: return emptyMap()
        val result = mutableMapOf<String, LocationPermissionData>()
        
        dataMap.forEach { (date, dateData) ->
            val dateMap = dateData as? Map<String, Any> ?: return@forEach
            val locationMap = dateMap["location"] as? Map<String, Any> ?: return@forEach
            val granted = (locationMap["granted"] as? Number)?.toInt() ?: 0
            
            result[date] = LocationPermissionData(
                location = LocationData(granted = granted)
            )
        }
        
        return result.toSortedMap(compareByDescending { it })
    }
    
    private fun parsePermissionStatusData(data: Map<String, Any>?): Map<String, PermissionStatusData> {
        if (data == null) return emptyMap()
        
        val dataMap = data["data"] as? Map<String, Any> ?: return emptyMap()
        val result = mutableMapOf<String, PermissionStatusData>()
        
        dataMap.forEach { (date, dateData) ->
            val dateMap = dateData as? Map<String, Any> ?: return@forEach
            val granted = (dateMap["granted"] as? Number)?.toInt() ?: 0
            
            result[date] = PermissionStatusData(granted = granted)
        }
        
        return result.toSortedMap(compareByDescending { it })
    }
    
    private fun parseProfileUpdatesData(data: Map<String, Any>?): Map<String, ProfileUpdateData> {
        if (data == null) return emptyMap()
        
        val dataMap = data["data"] as? Map<String, Any> ?: return emptyMap()
        val result = mutableMapOf<String, ProfileUpdateData>()
        
        dataMap.forEach { (date, dateData) ->
            val dateMap = dateData as? Map<String, Any> ?: return@forEach
            val avg = (dateMap["avg"] as? Number)?.toInt() ?: 0
            val total = (dateMap["total"] as? Number)?.toInt() ?: 0
            val users = (dateMap["users"] as? Number)?.toInt() ?: 0
            
            result[date] = ProfileUpdateData(avg = avg, total = total, users = users)
        }
        
        return result.toSortedMap(compareByDescending { it })
    }
    
    private fun parseScreenViewsData(data: Map<String, Any>?): Map<String, List<ScreenViewData>> {
        if (data == null) return emptyMap()
        
        val dataMap = data["data"] as? Map<String, Any> ?: return emptyMap()
        val result = mutableMapOf<String, List<ScreenViewData>>()
        
        dataMap.forEach { (date, dateData) ->
            val screenViewsList = dateData as? List<Map<String, Any>> ?: return@forEach
            val screenViews = screenViewsList.mapNotNull { screenViewMap ->
                val name = screenViewMap["name"] as? String ?: return@mapNotNull null
                val views = (screenViewMap["views"] as? Number)?.toInt() ?: 0
                ScreenViewData(name = name, views = views)
            }
            
            result[date] = screenViews
        }
        
        return result.toSortedMap(compareByDescending { it })
    }
}
