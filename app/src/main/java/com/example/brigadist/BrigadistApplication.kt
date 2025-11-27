package com.example.brigadist

import android.app.Application
import android.content.ComponentCallbacks2
import com.example.brigadist.cache.ImageCacheManager
import com.example.brigadist.cache.VideoCacheManager
import com.example.brigadist.ui.news.data.repository.NewsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BrigadistApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        // Firebase offline persistence removed - using SQLite for local storage instead
        
        // Preload news in background thread when app starts
        applicationScope.launch {
            try {
                val newsRepository = NewsRepository(this@BrigadistApplication)
                newsRepository.preloadNews()
            } catch (e: Exception) {
                // Silently handle errors - news will load when needed
            }
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        // Clear memory caches when system is low on memory
        ImageCacheManager.getInstance(this).clearMemoryCache()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Clear caches when system requests memory trimming
        // TRIM_MEMORY_MODERATE = 60
        if (level >= 60) {
            ImageCacheManager.getInstance(this).clearMemoryCache()
        }
    }
}

