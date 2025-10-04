package com.example.brigadist.data

import com.example.brigadist.ui.videos.model.Video
import com.google.firebase.database.*

class FirebaseVideoAdapter {

    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("videos")

    fun getVideos(callback: (List<Video>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val videos = mutableListOf<Video>()

                for (child in snapshot.children) {
                    // Caso 1: nodo es un string (URL)
                    val stringValue = child.getValue(String::class.java)
                    if (stringValue != null) {
                        videos.add(
                            Video(
                                id = child.key ?: "",
                                title = "Video ${child.key}",
                                url = stringValue
                            )
                        )
                        continue
                    }

                    // Caso 2: nodo es un objeto con campos
                    val videoObj = child.getValue(Video::class.java)
                    if (videoObj != null) {
                        videos.add(videoObj)
                    }
                }

                callback(videos)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error opcional
                callback(emptyList())
            }
        })
    }
}
