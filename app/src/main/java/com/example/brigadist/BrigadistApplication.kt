package com.example.brigadist

import android.app.Application
import android.content.ComponentCallbacks2
import com.example.brigadist.cache.ImageCacheManager
import com.example.brigadist.cache.VideoCacheManager
import com.google.firebase.database.FirebaseDatabase

class BrigadistApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable Firebase offline persistence BEFORE any database references are created
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Already enabled or error, continue
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

