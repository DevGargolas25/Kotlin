package com.example.brigadist.util

import android.content.Context

object VoteLimiter {
    private const val PREF_NAME = "vote_limiter"
    private const val VOTE_PREFIX = "voted_"

    fun hasVoted(context: Context, newsId: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastVote = prefs.getLong(VOTE_PREFIX + newsId, 0L)
        val tenDaysMillis = 10 * 24 * 60 * 60 * 1000L
        return System.currentTimeMillis() - lastVote < tenDaysMillis
    }

    fun saveVote(context: Context, newsId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(VOTE_PREFIX + newsId, System.currentTimeMillis()).apply()
    }
}
