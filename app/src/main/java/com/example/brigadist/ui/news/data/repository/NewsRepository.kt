package com.example.brigadist.ui.news.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.brigadist.data.FirebaseNewsAdapter
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
    
    // SharedPreferences to track if initial load has been completed
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "news_repository_prefs",
        Context.MODE_PRIVATE
    )
    private val keyInitialLoadCompleted = "initial_load_completed"
    
    private fun isInitialLoadCompleted(): Boolean {
        return prefs.getBoolean(keyInitialLoadCompleted, false)
    }
    
    private fun setInitialLoadCompleted() {
        prefs.edit().putBoolean(keyInitialLoadCompleted, true).apply()
    }
    
    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Get news from Firebase if online, otherwise from local database
     * Only stores news to local database during initial load (in preloadNews)
     * Subsequent updates from Firebase are NOT stored locally
     */
    fun getNews(): Flow<List<News>> {
        return if (isOnline()) {
            // Online: Always fetch from Firebase
            // Do NOT save to local database - only initial load does that
            firebaseAdapter.getNews()
        } else {
            // Offline: Fetch from local database (only contains initial load data)
            dao.getAllNews().map { entities ->
                entities.map { it.toNews() }
            }
        }
    }
    
    /**
     * Preload news in background thread
     * This should be called when the app starts
     * Only stores news to local database during the FIRST load
     * After initial load is complete, subsequent updates are NOT stored
     */
    fun preloadNews() {
        scope.launch {
            try {
                if (isOnline() && !isInitialLoadCompleted()) {
                    // Only do initial load if not already completed
                    // Get the first value from Firebase and save it, then stop
                    val newsList = firebaseAdapter.getNews().first()
                    try {
                        // Save initial news to local database
                        val entities = newsList.map { NewsEntity.fromNews(it) }
                        dao.insertAllNews(entities)
                        
                        // Mark initial load as completed
                        setInitialLoadCompleted()
                    } catch (e: Exception) {
                        // Ignore save errors
                    }
                }
                // If offline or initial load already completed, do nothing
                // News are already in local database from initial load
            } catch (e: Exception) {
                // Handle error silently - will fallback to local data
            }
        }
    }
    
    /**
     * Get news synchronously from local database (for initial load)
     */
    suspend fun getNewsSync(): List<News> = withContext(Dispatchers.IO) {
        dao.getAllNewsSync().map { it.toNews() }
    }
}

