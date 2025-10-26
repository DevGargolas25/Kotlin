package com.example.brigadist.data

import com.example.brigadist.ui.videos.model.Video
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseVideoAdapter {

    private val database = FirebaseDatabase.getInstance().getReference("Video")

    fun getVideos(): Flow<List<Video>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val videos = snapshot.children.mapNotNull {
                    val video = it.getValue(Video::class.java)
                    video?.copy(id = it.key ?: "")
                }
                trySend(videos)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }

    fun incrementViewCount(videoId: String) {
        database.child(videoId).child("views").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentValue = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentValue + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                // Transaction completed
            }
        })
    }

    fun toggleLike(videoId: String, userId: String) {
        database.child(videoId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val likeCountNode = currentData.child("like")
                val userLikeNode = currentData.child("likes").child(userId)

                var likeCount = likeCountNode.getValue(Int::class.java) ?: 0

                if (userLikeNode.value == null) {
                    // User hasn't liked it yet. Like it.
                    likeCount++
                    userLikeNode.value = true
                } else {
                    // User has liked it. Unlike it.
                    likeCount--
                    userLikeNode.value = null // Remove the user's like
                }

                // Set the new like count
                likeCountNode.value = likeCount

                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                // Transaction completed. Can log errors here if needed.
            }
        })
    }
}
