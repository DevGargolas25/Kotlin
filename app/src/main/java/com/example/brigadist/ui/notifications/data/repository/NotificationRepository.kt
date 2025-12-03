package com.example.brigadist.ui.notifications.data.repository

import android.content.Context
import com.example.brigadist.ui.notifications.data.local.NotificationDatabase
import com.example.brigadist.ui.notifications.data.local.entity.NotificationEntity
import com.example.brigadist.ui.notifications.data.remote.FirebaseNotificationsAdapter
import com.example.brigadist.ui.notifications.model.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Single source of truth repository:
 * - exposes Flow from local DB (Room)
 * - provides syncFromRemote() to keep DB updated with Firebase
 * - provides methods to modify remote data
 */
class NotificationRepository(context: Context) {

    private val db = NotificationDatabase.getInstance(context)
    private val dao = db.notificationDao()
    private val adapter = FirebaseNotificationsAdapter()

    /**
     * Flow of notifications from local DB (source of truth for UI)
     */
    fun observeNotificationsFlow(): Flow<List<Notification>> {
        return dao.observeAll().map { entities ->
            // Map database entities to UI models
            entities.map { entity ->
                Notification(
                    id = entity.id,
                    title = entity.title,
                    message = entity.message,
                    timestamp = entity.timestamp,
                    read = entity.read
                )
            }
        }
    }

    /**
     * Adds a single notification to Firebase and the local database simultaneously.
     * This provides instant UI feedback and ensures data is synced to the cloud.
     */
    suspend fun addNotification(notification: Notification) {
        // Write to local DB first for instant UI update.
        withContext(Dispatchers.IO) {
            val entity = NotificationEntity(
                id = if (notification.id.isNotBlank()) notification.id else "local_${notification.timestamp}",
                title = notification.title,
                message = notification.message,
                timestamp = notification.timestamp,
                read = notification.read
            )
            dao.upsertAll(listOf(entity))
        }
        // Then, push to remote. Firebase handles offline queueing.
        adapter.addNotification(notification)
    }

    /**
     * Synchronize remote -> local. Should be called from a background coroutine.
     * This function collects the remote adapter's flow and upserts into Room.
     */
    suspend fun syncFromRemote() {
        // Collect the remote flow and upsert into the local DB
        adapter.observeNotifications().collect { remoteList ->
            // Convert UI models from remote to database entities
            val entities = remoteList.map { notification ->
                NotificationEntity(
                    id = if (notification.id.isNotBlank()) notification.id else "${notification.timestamp}",
                    title = notification.title,
                    message = notification.message,
                    timestamp = notification.timestamp,
                    read = notification.read
                )
            }
            // Perform database write operation on the IO dispatcher
            withContext(Dispatchers.IO) {
                dao.upsertAll(entities)
            }
        }
    }

    /**
     * Marks a notification as read in the local database on a background thread.
     */
    suspend fun markAsRead(id: String) {
        withContext(Dispatchers.IO) {
            dao.markAsRead(id)
        }
    }

    /**
     * Deletes old notifications from the local database on a background thread.
     */
    suspend fun deleteOlderThan(cutoff: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteOlderThan(cutoff)
        }
    }
}
