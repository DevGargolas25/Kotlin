package com.example.brigadist.data

import com.example.brigadist.ui.news.model.News
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseNewsAdapter {

    private val database = FirebaseDatabase.getInstance().getReference("news")

    fun getNews(): Flow<List<News>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newsList = snapshot.children.mapNotNull {
                    val news = it.getValue(News::class.java)
                    news?.copy(id = it.key ?: "")
                }
                trySend(newsList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }
}

