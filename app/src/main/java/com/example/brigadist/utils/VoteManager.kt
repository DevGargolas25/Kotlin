package com.example.brigadist.utils

import android.content.Context
import android.content.SharedPreferences

class VoteManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("votes_prefs", Context.MODE_PRIVATE)

    fun saveVoteTime(newsId: String) {
        val now = System.currentTimeMillis()
        prefs.edit().putLong("vote_$newsId", now).apply()
    }

    fun canVote(newsId: String): Boolean {
        val lastVote = prefs.getLong("vote_$newsId", 0L)
        if (lastVote == 0L) return true

        val tenDaysMs = 10 * 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()

        return (now - lastVote) > tenDaysMs
    }
}
