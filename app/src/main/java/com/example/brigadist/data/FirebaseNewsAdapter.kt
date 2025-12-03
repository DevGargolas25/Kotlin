package com.example.brigadist.ui.news.data

import com.example.brigadist.ui.news.model.News
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseNewsAdapter {

    private val dbRef = FirebaseDatabase.getInstance().reference.child("news")

    fun getNews(): Flow<List<News>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<News>()
                snapshot.children.forEach { child ->
                    try {
                        val id = child.key ?: return@forEach
                        val title = child.child("title").getValue(String::class.java) ?: ""
                        val description = child.child("description").getValue(String::class.java) ?: ""
                        val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
                        val useful = child.child("usefulCount").getValue(Int::class.java) ?: 0
                        val notUseful = child.child("notUsefulCount").getValue(Int::class.java) ?: 0
                        val tagsList = child.child("tags").children.mapNotNull { it.getValue(String::class.java) }
                        list.add(News(id, title, description, imageUrl, tagsList, useful, notUseful))
                    } catch (_: Exception) { /* ignore malformed node */ }
                }
                trySend(list).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }
}
