package com.example.brigadist.cache

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Custom video cache manager with LRU eviction
 * Replaces ExoPlayer's SimpleCache
 */
class VideoCacheManager private constructor(context: Context) {
    
    private val cacheDir: File
    private val maxCacheSize: Long = 70 * 1024 * 1024 // 70 MB
    private val accessOrder = mutableListOf<String>() // LRU tracking
    private val cacheMetadata = ConcurrentHashMap<String, CacheEntry>()
    private val accessOrderLock = Any()
    
    data class CacheEntry(
        val key: String,
        val file: File,
        val size: Long,
        var lastAccessed: Long = System.currentTimeMillis()
    )
    
    init {
        cacheDir = File(context.cacheDir, "video_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        loadCacheMetadata()
        cleanCacheIfNeeded()
    }
    
    companion object {
        private const val TAG = "VideoCacheManager"
        
        @Volatile
        private var INSTANCE: VideoCacheManager? = null
        
        fun getInstance(context: Context): VideoCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VideoCacheManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Get cached video file or download and cache it
     */
    suspend fun getCachedVideoFile(videoUrl: String): File? {
        return withContext(Dispatchers.IO) {
            val key = urlToKey(videoUrl)
            val cacheFile = File(cacheDir, key)
            
            // Check if already cached
            if (cacheFile.exists() && cacheFile.length() > 0) {
                updateAccessTime(key)
                return@withContext cacheFile
            }
            
            // Download and cache
            downloadAndCache(videoUrl, key, cacheFile)
        }
    }
    
    /**
     * Download video and save to cache
     */
    private suspend fun downloadAndCache(
        videoUrl: String,
        key: String,
        cacheFile: File
    ): File? {
        return try {
            val connection = URL(videoUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 60000
            connection.requestMethod = "GET"
            
            val totalSize = connection.contentLength.toLong()
            var downloadedSize = 0L
            
            connection.inputStream.use { input ->
                FileOutputStream(cacheFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead
                    }
                }
            }
            
            // Update metadata
            synchronized(accessOrderLock) {
                if (key !in accessOrder) {
                    accessOrder.add(0, key)
                }
                cacheMetadata[key] = CacheEntry(key, cacheFile, cacheFile.length())
            }
            
            // Clean cache if needed
            cleanCacheIfNeeded()
            
            cacheFile
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading video: $videoUrl", e)
            cacheFile.delete()
            null
        }
    }
    
    /**
     * Append data to existing cache file (for progressive download)
     */
    suspend fun appendToCache(videoUrl: String, data: ByteArray, offset: Long) {
        withContext(Dispatchers.IO) {
            val key = urlToKey(videoUrl)
            val cacheFile = File(cacheDir, key)
            
            RandomAccessFile(cacheFile, "rw").use { file ->
                file.seek(offset)
                file.write(data)
            }
            
            updateAccessTime(key)
        }
    }
    
    /**
     * Check if video is partially cached and return available range
     */
    suspend fun getCachedRange(videoUrl: String): LongRange? {
        return withContext(Dispatchers.IO) {
            val key = urlToKey(videoUrl)
            val cacheFile = File(cacheDir, key)
            
            if (cacheFile.exists() && cacheFile.length() > 0) {
                0L..cacheFile.length()
            } else {
                null
            }
        }
    }
    
    /**
     * Update access time for LRU
     */
    private fun updateAccessTime(key: String) {
        synchronized(accessOrderLock) {
            accessOrder.remove(key)
            accessOrder.add(0, key)
            cacheMetadata[key]?.lastAccessed = System.currentTimeMillis()
        }
    }
    
    /**
     * Load cache metadata from disk
     */
    private fun loadCacheMetadata() {
        cacheDir.listFiles()?.forEach { file ->
            val key = file.name
            cacheMetadata[key] = CacheEntry(key, file, file.length(), file.lastModified())
            synchronized(accessOrderLock) {
                if (key !in accessOrder) {
                    accessOrder.add(key)
                }
            }
        }
    }
    
    /**
     * Clean cache using LRU eviction
     */
    private fun cleanCacheIfNeeded() {
        synchronized(accessOrderLock) {
            var totalSize = cacheMetadata.values.sumOf { it.size }
            
            if (totalSize <= maxCacheSize) return
            
            // Remove oldest files (LRU)
            while (totalSize > maxCacheSize && accessOrder.isNotEmpty()) {
                val oldestKey = accessOrder.removeAt(accessOrder.size - 1)
                val entry = cacheMetadata.remove(oldestKey)
                
                entry?.let {
                    totalSize -= it.size
                    it.file.delete()
                    Log.d(TAG, "Evicted video from cache: ${it.key}")
                }
            }
        }
    }
    
    /**
     * Convert URL to cache key
     */
    private fun urlToKey(url: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest(url.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Clear all cached videos
     */
    fun clearCache() {
        synchronized(accessOrderLock) {
            cacheDir.listFiles()?.forEach { it.delete() }
            accessOrder.clear()
            cacheMetadata.clear()
        }
    }
    
    /**
     * Get cache size
     */
    fun getCacheSize(): Long {
        return cacheMetadata.values.sumOf { it.size }
    }
}

