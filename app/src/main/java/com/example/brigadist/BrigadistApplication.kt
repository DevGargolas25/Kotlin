package com.example.brigadist

import android.app.Application
import android.content.ComponentCallbacks2
import com.example.brigadist.cache.ImageCacheManager
import com.example.brigadist.cache.VideoCacheManager

class BrigadistApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Firebase offline persistence removed - using SQLite for local storage instead
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

