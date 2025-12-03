package com.example.brigadist.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadist.ui.notifications.data.repository.NotificationRepository
import com.example.brigadist.ui.notifications.model.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * AndroidViewModel to get the Application context for the repository.
 * Exposes StateFlow<List<Notification>> to the UI and handles sync and actions.
 */
class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = NotificationRepository(application.applicationContext)

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        // Start observing the local DB (source of truth). The flow from Room will automatically
        // update the UI whenever the data changes.
        viewModelScope.launch {
            _loading.value = true
            repo.observeNotificationsFlow().collect { list ->
                _notifications.value = list
                _loading.value = false // Consider loading finished once first data is emitted
            }
        }

        // Start syncing from the remote source in a background thread.
        // This coroutine will run for the entire lifecycle of the ViewModel and continuously
        // listen for remote changes.
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // syncFromRemote is a long-running collecting job
                repo.syncFromRemote()
            } catch (e: Exception) {
                // Handle potential exceptions from the remote source
                e.printStackTrace()
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.markAsRead(id)
        }
    }

    fun clearOld(beforeTimestamp: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteOlderThan(beforeTimestamp)
        }
    }
}
