package com.example.brigadist.ui.notifications.data.remote

import com.example.brigadist.ui.notifications.model.Notification
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Adapter that listens to Realtime Database "notifications" node and emits List<Notification>.
 * Also provides a method to write notifications to Firebase.
 */
class FirebaseNotificationsAdapter {
    private val dbRef = FirebaseDatabase.getInstance().getReference("notifications")

    fun observeNotifications(): Flow<List<Notification>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val n = child.getValue(Notification::class.java)
                    // Use the Firebase key as the unique ID
                    n?.copy(id = child.key ?: n.id)
                }
                trySend(list).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        dbRef.addValueEventListener(listener)
        // This block is called when the flow is cancelled, ensuring the listener is removed
        awaitClose { dbRef.removeEventListener(listener) }
    }

    /**
     * Pushes a new notification to the Firebase Realtime Database.
     * Firebase will generate a unique key for the new entry.
     */
    fun addNotification(notification: Notification) {
        // Use push() to generate a unique key. Firebase handles offline queueing.
        dbRef.push().setValue(notification)
    }
}
