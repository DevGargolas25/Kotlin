package com.example.brigadist.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Transaction

/**
 * Minimal helper to increment global like/dislike counters under
 * Realtime Database path: AsistantLikes/{Likes, Dislikes}
 */
object AssistantLikesRepository {

    private const val ROOT = "AsistantLikes"

    fun incrementLike() {
        val ref = FirebaseDatabase.getInstance().getReference(ROOT).child("Likes")
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): Transaction.Result {
                val currentValue = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentValue + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                // no-op
            }
        })
    }

    fun incrementDislike() {
        val ref = FirebaseDatabase.getInstance().getReference(ROOT).child("Dislikes")
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): Transaction.Result {
                val currentValue = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentValue + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                // no-op
            }
        })
    }
}


