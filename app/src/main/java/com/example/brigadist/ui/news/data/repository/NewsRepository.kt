package com.example.brigadist.ui.news.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.brigadist.ui.news.data.FirebaseNewsAdapter
import com.example.brigadist.ui.news.data.local.NewsDatabase
import com.example.brigadist.ui.news.data.local.entity.NewsEntity
import com.example.brigadist.ui.news.model.News
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsRepository(private val context: Context) {
    private val firebaseAdapter = FirebaseNewsAdapter()
    private val database = NewsDatabase.getDatabase(context)
    private val dao = database.newsDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // SharedPreferences to track initial preload
    private val prefs: SharedPreferences = context.getSharedPreferences("news_repository_prefs", Context.MODE_PRIVATE)
    private val keyInitialLoad = "initial_load_completed"

    private fun isInitialLoadCompleted(): Boolean = prefs.getBoolean(keyInitialLoad, false)
    private fun markInitialLoad() = prefs.edit().putBoolean(keyInitialLoad, true).apply()

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(nw) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Returns a Flow of News. If online, uses Firebase realtime updates.
     * If offline, reads from Room local DB (initial preload).
     */
    fun getNews(): Flow<List<News>> {
        return if (isOnline()) {
            firebaseAdapter.getNews()
        } else {
            dao.getAllNews().map { entities -> entities.map { it.toNews() } }
        }
    }

    /**
     * Preload initial snapshot from Firebase into local DB (only once).
     */
    fun preloadNews() {
        scope.launch {
            try {
                if (isOnline() && !isInitialLoadCompleted()) {
                    val list = firebaseAdapter.getNews().first()
                    val entities = list.map { NewsEntity.fromNews(it) }
                    dao.insertAllNews(entities)
                    markInitialLoad()
                }
            } catch (_: Exception) { /* ignore */ }
        }
    }

    suspend fun getNewsSync(): List<News> = withContext(Dispatchers.IO) {
        dao.getAllNewsSync().map { it.toNews() }
    }
}
